package app.what.schedule.rksi

import app.what.schedule.core.cache.FileCache
import app.what.schedule.core.cache.NoOpFileCache
import app.what.schedule.core.clients.ScheduleClient
import app.what.schedule.core.models.*
import app.what.schedule.rksi.parser.RKSIGoogleDriveParser
import app.what.schedule.rksi.parser.RKSIReplacementsParser
import app.what.schedule.rksi.parser.XlsxReader
import app.what.schedule.rksi.parser.files
import app.what.schedule.rksi.parser.folders
import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

object RKSILessonsSchedule {
    val COMMON = listOf(
        LessonTimeDto(1, LocalTime(8, 0), LocalTime(9, 30)),
        LessonTimeDto(2, LocalTime(9, 40), LocalTime(11, 10)),
        LessonTimeDto(3, LocalTime(11, 30), LocalTime(13, 0)),
        LessonTimeDto(4, LocalTime(13, 10), LocalTime(14, 40)),
        LessonTimeDto(5, LocalTime(15, 0), LocalTime(16, 30)),
        LessonTimeDto(6, LocalTime(16, 40), LocalTime(18, 10)),
        LessonTimeDto(7, LocalTime(18, 20), LocalTime(19, 50))
    )

    val SHORTENED = listOf(
        LessonTimeDto(1, LocalTime(8, 0), LocalTime(8, 50)),
        LessonTimeDto(2, LocalTime(9, 0), LocalTime(9, 50)),
        LessonTimeDto(3, LocalTime(10, 0), LocalTime(10, 50)),
        LessonTimeDto(4, LocalTime(11, 0), LocalTime(11, 50)),
        LessonTimeDto(5, LocalTime(12, 0), LocalTime(12, 50)),
        LessonTimeDto(6, LocalTime(13, 0), LocalTime(13, 50)),
        LessonTimeDto(7, LocalTime(14, 0), LocalTime(14, 50))
    )

    val WITH_CLASS_HOUR = listOf(
        LessonTimeDto(1, LocalTime(8, 0), LocalTime(9, 30)),
        LessonTimeDto(2, LocalTime(9, 40), LocalTime(11, 10)),
        LessonTimeDto(3, LocalTime(11, 30), LocalTime(13, 0)),
        LessonTimeDto(0, LocalTime(13, 5), LocalTime(14, 5)),
        LessonTimeDto(4, LocalTime(14, 10), LocalTime(15, 40)),
        LessonTimeDto(5, LocalTime(16, 0), LocalTime(17, 30)),
        LessonTimeDto(6, LocalTime(17, 40), LocalTime(19, 10))
    )
}

class RKSIScheduleClient(
    private val client: HttpClient,
    private val baseUrl: String = "https://www.rksi.ru",
    private val googleDriveParser: RKSIGoogleDriveParser = RKSIGoogleDriveParser(client),
    private val fileCache: FileCache = NoOpFileCache(),
    private val xlsxReader: XlsxReader? = null,
    private val log: ((String) -> Unit)? = null
) : ScheduleClient {

    private val months = listOf(
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    )

    private fun parseMonth(month: String): Int = months.indexOfFirst { it.startsWith(month.take(3), ignoreCase = true) } + 1

    private fun parseTime(time: String): LocalTime {
        val parts = time.split(":").map { it.trim().toInt() }
        return LocalTime(parts[0], parts[1])
    }

    override suspend fun getTeachers(): List<TeacherDto> {
        val response = client.get("$baseUrl/mobileschedule/teachers").bodyAsText()
        return Ksoup.parse(response).select("a[href*=\"teachers\"]")
            .map { TeacherDto(id = it.attr("href").split("/").last(), name = it.text().trim()) }
            .distinctBy { it.name.trim() }
            .sortedBy { it.name }
    }

    override suspend fun getGroups(): List<GroupDto> {
        val response = client.get("$baseUrl/mobileschedule/groups").bodyAsText()
        return Ksoup.parse(response).select("a[href*=\"groups\"]")
            .map { GroupDto(id = it.attr("href").split("/").last(), name = it.text().trim()) }
            .distinctBy { it.name.trim() }
            .sortedBy { it.name }
    }

    override suspend fun getGroupSchedule(group: String, showReplacements: Boolean): List<DayScheduleDto> =
        fetchSchedule(group, isGroup = true, showReplacements = showReplacements)

    override suspend fun getTeacherSchedule(teacher: String, showReplacements: Boolean): List<DayScheduleDto> =
        fetchSchedule(teacher, isGroup = false, showReplacements = showReplacements)

    private suspend fun fetchSchedule(
        target: String,
        isGroup: Boolean,
        showReplacements: Boolean
    ): List<DayScheduleDto> = coroutineScope {
        log?.invoke("Запрос расписания РКСИ для ${if (isGroup) "группы" else "преподавателя"}: $target")

        val url = "$baseUrl/mobileschedule/" + (if (isGroup) "groups/" else "teachers/") + target
        val response = client.get(url).bodyAsText()
        val document = Ksoup.parse(response)
        val targetName = document.selectFirst("h3")?.text()?.trim()?.ifEmpty { null } ?: target
        val dayElements = document.getElementsByClass("schedule_item")

        // Запуск фоновой подгрузки замен из Google Drive (неблокирующий)
        val replacementsDeferred: kotlinx.coroutines.Deferred<List<LessonDto>>? = if (showReplacements) {
            async(Dispatchers.Default) {
                try {
                    fetchReplacements(targetName, isGroup)
                } catch (e: Exception) {
                    log?.invoke("Ошибка получения замен: ${e.message}")
                    emptyList()
                }
            }
        } else null

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val daySchedules = dayElements.map { dayElement ->
            val titleText = dayElement.getElementsByClass("schedule_title").firstOrNull()?.text() ?: ""
            val dateParts = titleText.split(",").firstOrNull()?.trim()?.split(" ") ?: emptyList()
            val date = if (dateParts.size >= 2) {
                val dayNum = dateParts[0].toIntOrNull() ?: today.dayOfMonth
                val monthNum = parseMonth(dateParts[1]).takeIf { it in 1..12 } ?: today.monthNumber
                try {
                    LocalDate(today.year, monthNum, dayNum)
                } catch (_: Exception) {
                    today
                }
            } else {
                today
            }

            var lessons = dayElement.getElementsByTag("p").mapNotNull { lessonRaw ->
                if (lessonRaw.html().contains("href")) return@mapNotNull null
                val content = lessonRaw.html().split("<br>")
                if (content.size < 3) return@mapNotNull null

                val timeParts = content[0].split("—")
                val startTime = parseTime(timeParts.first().trim())
                val endTime = parseTime(timeParts.last().trim())
                val subject = content[1].replace("<.*?>".toRegex(), "").trim()

                val thirdLine = content[2].split(", ")
                val teacherOrGroup = thirdLine.firstOrNull()?.replace("<.*?>".toRegex(), "")?.trim() ?: ""
                val audBuilding = thirdLine.lastOrNull()?.split(" ")?.lastOrNull()?.split("/") ?: emptyList()

                val aud = if (audBuilding.size > 1) audBuilding.dropLast(1).joinToString("/") else audBuilding.firstOrNull() ?: ""
                val bld = audBuilding.lastOrNull() ?: "1"

                val isClassHour = "Классный" in subject
                val isAdditional = "Доп." in subject

                val otUnit = OneTimeUnitDto(
                    teacher = if (isGroup) teacherOrGroup else targetName,
                    group = if (isGroup) targetName else teacherOrGroup,
                    room = aud,
                    additional = "Корпус $bld"
                )

                LessonDto(
                    date = date,
                    number = 0,
                    startTime = startTime,
                    endTime = endTime,
                    subject = subject,
                    otUnits = if (isClassHour) emptyList() else listOf(otUnit),
                    type = when {
                        isClassHour -> LessonTypeDto.OTHER
                        isAdditional -> LessonTypeDto.PRACTICE
                        else -> LessonTypeDto.COMMON
                    }
                )
            }

            // Определение типа сетки звонков
            val scheduleType = when {
                lessons.any { it.subject.contains("Классный", ignoreCase = true) } -> LessonsScheduleTypeDto.WITH_CLASS_HOUR
                lessons.any { it.startTime == LocalTime(8, 0) && it.endTime == LocalTime(8, 50) } -> LessonsScheduleTypeDto.SHORTENED
                else -> LessonsScheduleTypeDto.COMMON
            }

            val timeSchedule = when (scheduleType) {
                LessonsScheduleTypeDto.SHORTENED -> RKSILessonsSchedule.SHORTENED
                LessonsScheduleTypeDto.WITH_CLASS_HOUR -> RKSILessonsSchedule.WITH_CLASS_HOUR
                else -> RKSILessonsSchedule.COMMON
            }

            lessons = lessons.mapIndexed { index, lesson ->
                val isClassHour = lesson.subject.contains("Классный", ignoreCase = true)
                if (isClassHour) {
                    lesson.copy(number = 0)
                } else {
                    val match = timeSchedule.firstOrNull { it.number != 0 && (it.start == lesson.startTime || it.end == lesson.endTime) }
                        ?: RKSILessonsSchedule.COMMON.firstOrNull { it.number != 0 && (it.start == lesson.startTime || it.end == lesson.endTime) }
                        ?: RKSILessonsSchedule.WITH_CLASS_HOUR.firstOrNull { it.number != 0 && (it.start == lesson.startTime || it.end == lesson.endTime) }
                        ?: RKSILessonsSchedule.SHORTENED.firstOrNull { it.number != 0 && (it.start == lesson.startTime || it.end == lesson.endTime) }

                    val number = match?.number ?: (index + 1)
                    lesson.copy(number = number)
                }
            }

            DayScheduleDto(
                date = date,
                scheduleType = scheduleType,
                lessons = lessons
            )
        }

        // Если замены подгрузились, накладываем их
        val replacements = replacementsDeferred?.await() ?: emptyList()
        if (replacements.isNotEmpty()) {
            daySchedules.map { daySchedule ->
                val dayReplacements = replacements.filter { it.date == daySchedule.date }
                if (dayReplacements.isNotEmpty()) {
                    val timeSchedule = when (daySchedule.scheduleType) {
                        LessonsScheduleTypeDto.SHORTENED -> RKSILessonsSchedule.SHORTENED
                        LessonsScheduleTypeDto.WITH_CLASS_HOUR -> RKSILessonsSchedule.WITH_CLASS_HOUR
                        else -> RKSILessonsSchedule.COMMON
                    }
                    val merged = RKSIReplacementsParser.applyReplacements(
                        daySchedule.lessons,
                        dayReplacements,
                        timeSchedule
                    )
                    daySchedule.copy(lessons = merged)
                } else daySchedule
            }
        } else {
            daySchedules
        }
    }

    private suspend fun fetchReplacements(target: String, isGroup: Boolean): List<LessonDto> {
        val folderId = resolveDriveFolderId()
        val rootItems = googleDriveParser.getFolderContent(folderId)
        val rootFiles = rootItems.files().onEach { it.additionalData["building"] = 1 }
        val subFolderId = rootItems.folders().firstOrNull()?.id
        val subItems = if (subFolderId != null) googleDriveParser.getFolderContent(subFolderId) else emptyList()
        val subFiles = subItems.files().onEach { it.additionalData["building"] = 2 }
        val allFiles = rootFiles + subFiles

        val cleanTarget = target.replace(" ", "").trim().lowercase()
        val predicate = { teacher: String, group: String ->
            if (isGroup) {
                val cleanGroup = group.replace(" ", "").trim().lowercase()
                cleanGroup == cleanTarget
            } else {
                val cleanTeacher = teacher.replace(" ", "").trim().lowercase()
                cleanTeacher.contains(cleanTarget) || cleanTarget.contains(cleanTeacher) ||
                    (target.split(" ").firstOrNull()?.let { cleanTeacher.contains(it.lowercase()) } == true)
            }
        }

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val relevantFiles = allFiles.filter { file ->
            val parts = file.name.split(".").take(2).mapNotNull { it.toIntOrNull() }
            if (parts.size == 2) {
                val fileDate = LocalDate(today.year, parts[1], parts[0])
                file.additionalData["date"] = fileDate
                fileDate >= today.minus(1, DateTimeUnit.DAY)
            } else false
        }

        val parsedLists = coroutineScope {
            relevantFiles.map { file ->
                async(Dispatchers.Default) {
                    val date = file.additionalData["date"] as? LocalDate ?: return@async null
                    val building = file.additionalData["building"] as? Int ?: 1
                    val columns = if (building == 1) 2 else 1

                    val cacheKey = "rksi_rep_b${building}_${date}.xlsx"
                    val cachedBytes = fileCache.get(cacheKey)

                    val bytes = if (cachedBytes != null) {
                        cachedBytes
                    } else {
                        try {
                            val downloaded = client.get(file.getDownloadLink()).readRawBytes()
                            fileCache.put(cacheKey, downloaded, ttlMillis = 12 * 60 * 60 * 1000L)
                            downloaded
                        } catch (e: Exception) {
                            log?.invoke("Ошибка скачивания планшетки $cacheKey: ${e.message}")
                            null
                        }
                    } ?: return@async null

                    try {
                        val reader = xlsxReader ?: return@async null
                        val sheets = reader.readSheets(bytes)
                        RKSIReplacementsParser.parse(sheets, columns, date, predicate)
                    } catch (e: Exception) {
                        log?.invoke("Ошибка парсинга планшетки $cacheKey: ${e.message}")
                        null
                    }
                }
            }.awaitAll()
        }

        return parsedLists.filterNotNull().flatten()
    }

    private suspend fun resolveDriveFolderId(): String {
        return try {
            val response = client.get("$baseUrl/schedule").bodyAsText()
            val document = Ksoup.parse(response)
            val tabletUrl = document.getElementsMatchingText("Планшетка").lastOrNull()?.attr("href")
            tabletUrl?.split("/")?.lastOrNull()?.substringBefore("?")?.ifEmpty { null }
                ?: "1kUYiSAafghhYR0ARyXwPW1HZPpHcFIag"
        } catch (_: Exception) {
            "1kUYiSAafghhYR0ARyXwPW1HZPpHcFIag"
        }
    }
}

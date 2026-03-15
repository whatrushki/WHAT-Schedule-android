package app.what.schedule.data.remote.providers.rksi.services

import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.asyncLazy
import app.what.schedule.data.remote.api.AdditionalData
import app.what.schedule.data.remote.api.ScheduleResponse
import app.what.schedule.data.remote.api.ScheduleService
import app.what.schedule.data.remote.api.models.DaySchedule
import app.what.schedule.data.remote.api.models.Group
import app.what.schedule.data.remote.api.models.Lesson
import app.what.schedule.data.remote.api.models.LessonState
import app.what.schedule.data.remote.api.models.LessonTime
import app.what.schedule.data.remote.api.models.LessonType
import app.what.schedule.data.remote.api.models.LessonsScheduleType
import app.what.schedule.data.remote.api.models.OneTimeUnit
import app.what.schedule.data.remote.api.models.ParseMode
import app.what.schedule.data.remote.api.models.ScheduleSearch
import app.what.schedule.data.remote.api.models.Teacher
import app.what.schedule.data.remote.providers.rksi.RKSILessonsSchedule
import app.what.schedule.data.remote.utils.parseMonth
import app.what.schedule.data.remote.utils.parseTime
import app.what.schedule.libs.FileManager
import app.what.schedule.libs.GoogleDriveParser
import app.what.schedule.libs.files
import app.what.schedule.libs.folders
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.fleeksoft.ksoup.Ksoup
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class RKSIScheduleService(
    private val baseUrl: String,
    private val client: HttpClient,
    private val googleDriveApi: GoogleDriveParser,
    private val fileManager: FileManager,
    scope: CoroutineScope,
    private val generateFileName: (
        additional: AdditionalData,
        fileExtension: String
    ) -> String
) : ScheduleService {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    private val scheduleTabletGoogleDriveId by scope.asyncLazy { getScheduleTabletGoogleDriveId() }

    override suspend fun getTeachers(): List<Teacher> {
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "rksi")
        Auditor.debug(netTag, "Загрузка списка преподавателей")

        val response = client.get("$baseUrl/mobileschedule/teachers").bodyAsText()
        val teachers = Ksoup.parse(response).select("a[href*=\"teachers\"]")
            .map { Teacher(it.text(), it.attr("href").split("/").last()) }

        Auditor.debug(netTag, "Загружено преподавателей: ${teachers.size}")
        return teachers
    }

    override suspend fun getGroups(): List<Group> {
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "rksi")
        Auditor.debug(netTag, "Загрузка списка групп")

        val response = client.get("$baseUrl/mobileschedule/groups").bodyAsText()
        val groups = Ksoup.parse(response).select("a[href*=\"groups\"]")
            .map { Group(it.text(), it.attr("href").split("/").last()) }

        Auditor.debug(netTag, "Загружено групп: ${groups.size}")
        return groups
    }

    override suspend fun getTeacherSchedule(
        teacher: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): ScheduleResponse = getAndParseSchedule(
        teacher,
        ParseMode.TEACHER,
        additional["requiresData"] as Boolean,
        additional["lastModified"] as LocalDateTime?,
        showReplacements
    )

    override suspend fun getGroupSchedule(
        group: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): ScheduleResponse = getAndParseSchedule(
        group,
        ParseMode.GROUP,
        additional["requiresData"] as Boolean,
        additional["lastModified"] as LocalDateTime?,
        showReplacements
    )

    private suspend fun getAndParseSchedule(
        value: String,
        parseMode: ParseMode,
        requiresData: Boolean,
        lastModified: LocalDateTime?,
        showReplacements: Boolean
    ): ScheduleResponse {
        val scheduleTag = buildTag(LogScope.SCHEDULE, LogCat.NET, "rksi")

        Auditor.debug(
            scheduleTag,
            "Запрос расписания для ${if (parseMode == ParseMode.GROUP) "группы" else "преподавателя"}: $value"
        )
        crashlytics.setCustomKey("schedule_request_type", parseMode.name)
        crashlytics.setCustomKey("schedule_request_value", value)
        crashlytics.setCustomKey("institution", "rksi")

        val files1 = googleDriveApi.getFolderContent(scheduleTabletGoogleDriveId.await())
        val files2 = googleDriveApi.getFolderContent(files1.folders().first().id)
        val files = (files1 + files2).files()

        Auditor.debug(
            scheduleTag,
            "Требуются данные: $requiresData, последнее изменение: $lastModified"
        )

        if (!requiresData && files.any { lastModified != null && it.lastModified > lastModified }
                .not()) {
            Auditor.debug(scheduleTag, "Расписание актуально, возврат UpToDate")
            return ScheduleResponse.UpToDate
        }

        val response = client.get(
            "$baseUrl/mobileschedule/" +
                    (if (parseMode == ParseMode.GROUP) "groups/" else "teachers/") +
                    value
        ).bodyAsText()

        var dataRaw: List<String>
        val daySchedulesRaw = Ksoup.parse(response).getElementsByClass("schedule_item")

        Auditor.debug(scheduleTag, "Найдено дней в расписании: ${daySchedulesRaw.size}")

        val replacements = CoroutineScope(IO).async {
            getAllReplacements(
                files1, files2, lastModified, when (parseMode) {
                    ParseMode.GROUP -> ScheduleSearch.Group(value)
                    ParseMode.TEACHER -> ScheduleSearch.Teacher(value)
                }
            )
        }

        val daySchedules = daySchedulesRaw.mapIndexed { index, it ->
            dataRaw =
                it.getElementsByClass("schedule_title")[0].text().split(", ").first().split(" ")
            val date = LocalDate.now()
                .withDayOfMonth(dataRaw.first().toInt())
                .withMonth(parseMonth(dataRaw.last()))

            var lessons: List<Lesson>
            lessons = it.getElementsByTag("p").mapNotNull { lessonRaw ->
                if (lessonRaw.html().contains("href")) return@mapNotNull null
                val content = lessonRaw.html().split("<br>")

                dataRaw = content.first().split("—")
                val startTime = parseTime(dataRaw.first().trim())
                val endTime = parseTime(dataRaw.last().trim())
                val subject = content[1].substring(3, content[1].length - 4)

                dataRaw = content.last().split(", ")
                val teacherOrGroup = dataRaw.first().substring(1)

                dataRaw = dataRaw.last().split(" ").last().split("/")

                val auditory = if (dataRaw.size == 1) dataRaw.first()
                else dataRaw.dropLast(1).joinToString("/")
                val building = dataRaw.last()

                val lessonType = when {
                    "Доп." in subject -> LessonType.ADDITIONAL
                    "Классный" in subject -> LessonType.CLASS_HOUR
                    else -> LessonType.COMMON
                }

                val otUnits = listOf(
                    if (lessonType != LessonType.CLASS_HOUR) OneTimeUnit(
                        group = Group(if (parseMode == ParseMode.GROUP) value else teacherOrGroup),
                        teacher = Teacher(if (parseMode == ParseMode.TEACHER) value else teacherOrGroup),
                        building = building,
                        auditory = auditory
                    ) else OneTimeUnit.empty()
                )

                Lesson(
                    date = date,
                    number = 0,
                    otUnits = otUnits,
                    startTime = startTime,
                    endTime = endTime,
                    subject = subject,
                    type = lessonType
                )
            }.toMutableList().apply {
                val groupedLessons = groupBy { it.startTime }
                clear()

                groupedLessons.forEach { (_, l) ->
                    var unionLesson = l.first()
                    l.forEachIndexed { index, it -> if (index != 0) unionLesson += it }
                    add(unionLesson)
                }
            }

            var schedule = RKSILessonsSchedule.COMMON

            val getNumberOrChangeSchedule = { time: LocalTime, change: List<LessonTime> ->
                schedule.firstOrNull { it.startTime == time }?.number ?: let {
                    schedule = change; null
                }
            }

            lessons = lessons.map { lesson ->
                val number = getNumberOrChangeSchedule(
                    lesson.startTime,
                    RKSILessonsSchedule.WITH_CLASS_HOUR
                ) ?: getNumberOrChangeSchedule(
                    lesson.startTime,
                    RKSILessonsSchedule.SHORTENED
                ) ?: getNumberOrChangeSchedule(
                    lesson.startTime,
                    RKSILessonsSchedule.COMMON
                ) ?: 0

                lesson.copy(number = number)
            }

            Auditor.debug(scheduleTag, "Обработано уроков для $date: ${lessons.size}")

            if (showReplacements && index < 2) {
                lessons = lessons
                    .withReplacements(replacements.await().filter { it.date == date }, schedule)
                    .sortedBy { it.startTime }
            }

            DaySchedule(
                date = date,
                lessons = lessons,
                scheduleType = when (schedule) {
                    RKSILessonsSchedule.COMMON -> LessonsScheduleType.COMMON
                    RKSILessonsSchedule.SHORTENED -> LessonsScheduleType.SHORTENED
                    RKSILessonsSchedule.WITH_CLASS_HOUR -> LessonsScheduleType.WITH_CLASS_HOUR
                    else -> LessonsScheduleType.COMMON
                }
            )
        }

        val maxModified = files.maxOf { it.lastModified }
        Auditor.debug(scheduleTag, "Расписание успешно получено, последнее изменение: $maxModified")
        return ScheduleResponse.Available.FromSource(daySchedules, maxModified)
    }

    private suspend fun getScheduleTabletGoogleDriveId(): String {
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "rksi")
        Auditor.debug(netTag, "Получение ID папки с расписанием из Google Drive")

        val response = client.get("https://www.rksi.ru/schedule").bodyAsText()
        val document = Ksoup.parse(response)
        val tabletUrl = document.getElementsMatchingText("Планшетка").last()!!.attr("href")
        val driveId = tabletUrl.split("/").last()

        Auditor.debug(netTag, "ID папки Google Drive: $driveId")
        crashlytics.setCustomKey("gdrive_folder_id", driveId)
        return driveId
    }

    private suspend fun getAllReplacements(
        itemsOfFirstBuilding: List<GoogleDriveParser.Item>,
        itemsOfSecondBuilding: List<GoogleDriveParser.Item>,
        lastModified: LocalDateTime?,
        search: ScheduleSearch
    ): List<Lesson> {
        val predicate = { teacher: String, group: String ->
            when (search) {
                is ScheduleSearch.Group -> group == search.name
                is ScheduleSearch.Teacher -> teacher == search.name
            }
        }

        val scheduleTag = buildTag(LogScope.SCHEDULE, LogCat.NET, "rksi")
        Auditor.debug(scheduleTag, "Начало получения замен из Google Drive")

        val currentDate = LocalDate.now()

        fun List<GoogleDriveParser.Item.File>.filterByModifiedDateAndPutData(shortYear: Boolean = false) =
            filter {
                val raw = it.name.split(".").dropLast(1).map(String::toInt)
                val date = LocalDate.of(LocalDate.now().year, raw[1], raw[0])
                (date >= currentDate).also { _ -> it.additionalData["date"] = date }
            }

        val building1TabletsParsingProcess =
            itemsOfFirstBuilding.files().filterByModifiedDateAndPutData()
                .map {
                    createTabletParsingAsyncTask(
                        it.additionalData["date"] as LocalDate,
                        lastModified, "1", 2, it, predicate
                    )
                }

        val building2TabletsParsingProcess =
            itemsOfSecondBuilding.files().filterByModifiedDateAndPutData(true)
                .map {
                    createTabletParsingAsyncTask(
                        it.additionalData["date"] as LocalDate,
                        lastModified, "2", 1, it, predicate
                    )
                }

        val tablet1Replacements = building1TabletsParsingProcess.awaitAll()
        val tablet2Replacements = building2TabletsParsingProcess.awaitAll()
        val totalReplacements =
            tablet1Replacements.filterNotNull().flatten() + tablet2Replacements.filterNotNull()
                .flatten()

        Auditor.debug(scheduleTag, "Всего найдено замен: ${totalReplacements.size}")
        return totalReplacements
    }

    private fun createTabletParsingAsyncTask(
        date: LocalDate,
        lastModified: LocalDateTime?,
        building: String,
        columns: Int,
        file: GoogleDriveParser.Item.File,
        predicate: (String, String) -> Boolean
    ) = CoroutineScope(IO).async {
        val scheduleTag = buildTag(LogScope.SCHEDULE, LogCat.NET, "rksi")
        val tablet =
            getTablet(
                date,
                building,
                lastModified != null && file.lastModified > lastModified,
                file
            ) ?: return@async null

        if (!tablet.exists() || !tablet.canRead()) {
            Auditor.warn(scheduleTag, "Файл планшетки недоступен: ${tablet.path}")
            return@async null
        }

        val workbook1 = tablet.inputStream().use { inputStream ->
            WorkbookFactory.create(inputStream)
        }

        val lessons = parseLessonsFromWorkbook(workbook1, columns, date, predicate)
        Auditor.debug(
            scheduleTag,
            "Обработано замен из планшетки здания $building для $date: ${lessons.size}"
        )
        return@async lessons
    }

    private suspend fun getTablet(
        date: LocalDate,
        building: String,
        fromDrive: Boolean,
        file: GoogleDriveParser.Item.File
    ): File? {
        val tabletName = generateFileName(
            mapOf(
                "building" to building,
                "date" to date.toString()
            ), "xlsx"
        )

        fun getCachedFile() = fileManager
            .getFile(FileManager.DirectoryType.CACHE, tabletName)
            .takeIf { it.exists() }

        suspend fun downloadAndGetCachedFile(): File? {
            val downloaded = findAndDownloadTabletFromGD(file, tabletName)
            return if (downloaded) getCachedFile() else null
        }

        return when {
            fromDrive -> downloadAndGetCachedFile()
            else -> getCachedFile() ?: downloadAndGetCachedFile()
        }
    }

    private suspend fun findAndDownloadTabletFromGD(
        file: GoogleDriveParser.Item.File,
        fileName: String
    ): Boolean {
        val fileTag = buildTag(LogScope.FILE, LogCat.NET, "rksi")
        try {
            Auditor.debug(fileTag, "Загрузка планшетки из Google Drive: $fileName")
            crashlytics.setCustomKey("tablet_file_name", fileName)

            val downloadedTablet = client.get(file.getDownloadLink()).readRawBytes()
            fileManager.writeBytes(
                FileManager.DirectoryType.CACHE,
                fileName,
                downloadedTablet
            )

            Auditor.debug(fileTag, "Планшетка успешно загружена: $fileName")
            return true
        } catch (e: Exception) {
            Auditor.err(fileTag, "Ошибка загрузки планшетки: $fileName", e)
            crashlytics.setCustomKey("tablet_download_error", fileName)
            crashlytics.recordException(e)
            return false
        }
    }

    private fun parseLessonsFromWorkbook(
        workbook: Workbook,
        columns: Int,
        date: LocalDate,
        predicate: (teacher: String, group: String) -> Boolean
    ): List<Lesson> {
        val lessons = mutableListOf<Lesson>()

        workbook.forEach { sheet ->
            var emptyRows = 0
            var rowIndex = -1

            val otUnits = mutableListOf<OneTimeUnit>()

            val lessonNumber =
                if ("Пара" !in sheet.sheetName) 0
                else sheet.sheetName.split(" ").last().toInt()

            for (row in sheet) {
                rowIndex++
                if (rowIndex == 0) continue
                if (emptyRows > 5) break

                if (row.lastCellNum < 0) emptyRows++
                else (0 until columns).mapIndexedNotNull { colIndex, i ->
                    val firstCellIndex = i * 3

                    val auditory = row.getCell(firstCellIndex)
                        ?.toString()
                        ?.ifEmpty { return@mapIndexedNotNull null }
                        ?: return@mapIndexedNotNull null

                    val teacher = row.getCell(firstCellIndex + 2)
                        ?.toString()
                        ?.ifEmpty { return@mapIndexedNotNull null }
                        ?.trim()
                        ?: return@mapIndexedNotNull null

                    val groups = row.getCell(firstCellIndex + 1)
                        ?.toString()
                        ?.split(if (columns == 1) "+" else ",")
                        ?.filter { predicate(teacher, it) }
                        ?.ifEmpty { return@mapIndexedNotNull null }
                        ?: return@mapIndexedNotNull null


                    groups.map {
                        OneTimeUnit(
                            auditory = try {
                                auditory.toFloat().toInt().toString()
                            } catch (e: Exception) {
                                auditory
                            },
                            group = Group(it.trim()),
                            teacher = Teacher(
                                teacher.replace(
                                    "__", "_"
                                ), teacher
                            ),
                            building = if (columns == 1) "2" else "1"
                        )
                    }
                }.let { otUnits.addAll(it.flatten()) }

                if (otUnits.isNotEmpty()) lessons.add(
                    Lesson(
                        date = date,
                        number = lessonNumber,
                        startTime = LocalTime.MIN,
                        endTime = LocalTime.MIN,
                        otUnits = otUnits,
                        subject = "",
                        type = when {
                            lessonNumber == 0 -> LessonType.CLASS_HOUR
                            else -> LessonType.COMMON
                        }
                    )
                )
            }
        }

        workbook.close()

        return lessons
    }
}

private fun List<Lesson>.withReplacements(
    replacements: List<Lesson>,
    schedule: List<LessonTime>
): List<Lesson> {
    val unionSchedule = mutableMapOf<Int, Pair<Lesson?, Lesson?>>()

    replacements.forEach {
        unionSchedule[it.number] = it to null
    }

    this.forEach {
        unionSchedule[it.number] = unionSchedule[it.number]?.first to it
    }

    return unionSchedule.map {
        val replacement = it.value.first
        val lesson = it.value.second

        return@map if (replacements.isNotEmpty()) {
            if (replacement == null && lesson != null)
                lesson.copy(state = LessonState.REMOVED)
            else if (replacement != null && lesson == null) {
                val lessonTime = schedule.firstOrNull { it.number == replacement.number }
                if (lessonTime == null) {
                    val scheduleTag = buildTag(LogScope.SCHEDULE, LogCat.STATE, "rksi")
                    Auditor.warn(
                        scheduleTag,
                        "Не найдено время для замены номер ${replacement.number}"
                    )
                }
                replacement.copy(
                    state = LessonState.ADDED,
                    startTime = lessonTime!!.startTime,
                    endTime = lessonTime.endTime,
                    subject = if (replacement.type == LessonType.CLASS_HOUR) "Классный час"
                    else replacement.subject
                )
            } else if (!lesson!!.equalsWithReplacement(replacement!!)) {
                val lessonTeachers = lesson.otUnits.map { it.teacher }

                replacement.copy(
                    number = lesson.number,
                    startTime = lesson.startTime,
                    endTime = lesson.endTime,
                    type = lesson.type,
                    state = if (lesson.type != LessonType.CLASS_HOUR) LessonState.CHANGED
                    else LessonState.COMMON,
                    subject = if (
                        lesson.type != LessonType.CLASS_HOUR &&
                        replacement.otUnits.all { it.teacher !in lessonTeachers }
                    ) "" else lesson.subject
                )
            } else lesson
        } else lesson!!
    }
}
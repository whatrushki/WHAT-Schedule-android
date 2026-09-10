package app.what.schedule.iubip

import app.what.schedule.core.clients.ScheduleClient
import app.what.schedule.core.models.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.parameters
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.ConcurrentHashMap

object IUBIPLessonsSchedule {
    val COMMON = listOf(
        LessonTimeDto(1, LocalTime.of(8, 20), LocalTime.of(9, 50)),
        LessonTimeDto(2, LocalTime.of(10, 0), LocalTime.of(11, 30)),
        LessonTimeDto(3, LocalTime.of(11, 40), LocalTime.of(13, 10)),
        LessonTimeDto(4, LocalTime.of(13, 30), LocalTime.of(15, 0)),
        LessonTimeDto(5, LocalTime.of(15, 10), LocalTime.of(16, 40)),
        LessonTimeDto(6, LocalTime.of(17, 0), LocalTime.of(18, 30)),
        LessonTimeDto(7, LocalTime.of(18, 40), LocalTime.of(20, 10)),
        LessonTimeDto(8, LocalTime.of(20, 20), LocalTime.of(21, 50))
    )
}

class IUBIPScheduleClient(
    private val client: HttpClient,
    private val baseUrl: String = "https://www.iubip.ru",
    private val log: ((String) -> Unit)? = null
) : ScheduleClient {

    private val cachedTeachers = ConcurrentHashMap.newKeySet<String>()

    override suspend fun getGroups(): List<GroupDto> {
        return try {
            val response = client.submitForm(
                url = "$baseUrl/local/templates/univer/include/schedule/ajax/read-file-groups.php",
                formParameters = parameters {
                    append("do", "groups")
                }
            )

            val text = response.bodyAsText()
            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            val element = json.parseToJsonElement(text)
            val groups = mutableListOf<GroupDto>()

            if (element is JsonObject) {
                element.values.forEach { facultyElem ->
                    if (facultyElem is JsonObject) {
                        facultyElem.entries.forEach { (groupName, courseElem) ->
                            val course = courseElem.jsonPrimitive.intOrNull ?: 1
                            val cleanName = groupName.trim()
                            if (cleanName.isNotBlank()) {
                                groups.add(GroupDto(id = cleanName, name = cleanName, course = course))
                            }
                        }
                    }
                }
            }

            groups.distinctBy { it.name }.sortedBy { it.name }
        } catch (e: Exception) {
            log?.invoke("Ошибка загрузки групп ИУБиП: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getTeachers(): List<TeacherDto> {
        if (cachedTeachers.isNotEmpty()) {
            return cachedTeachers.map { TeacherDto(id = it, name = it) }.sortedBy { it.name }
        }

        return try {
            val groups = getGroups().take(20)
            groups.forEach { group ->
                getGroupSchedule(group.name, false)
            }
            cachedTeachers.map { TeacherDto(id = it, name = it) }.sortedBy { it.name }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getTeacherSchedule(
        teacher: String,
        showReplacements: Boolean
    ): List<DayScheduleDto> {
        val groups = getGroups().take(30)
        val allDays = mutableMapOf<LocalDate, MutableList<LessonDto>>()

        groups.forEach { group ->
            val groupDays = getGroupSchedule(group.name, showReplacements)
            groupDays.forEach { day ->
                val teacherLessons = day.lessons.filter { lesson ->
                    lesson.otUnits.any { it.teacher.equals(teacher, ignoreCase = true) }
                }
                if (teacherLessons.isNotEmpty()) {
                    allDays.getOrPut(day.date) { mutableListOf() }.addAll(teacherLessons)
                }
            }
        }

        return allDays.map { (date, lessons) ->
            DayScheduleDto(
                date = date,
                scheduleType = LessonsScheduleTypeDto.COMMON,
                lessons = lessons.distinctBy { it.number to it.subject }.sortedBy { it.number }
            )
        }.sortedBy { it.date }
    }

    override suspend fun getGroupSchedule(
        group: String,
        showReplacements: Boolean
    ): List<DayScheduleDto> {
        return try {
            val response = client.submitForm(
                url = "$baseUrl/local/templates/univer/include/schedule/ajax/read-file-groups.php",
                formParameters = parameters {
                    append("do", "schedule")
                    append("group", group.trim())
                }
            )

            val text = response.bodyAsText()
            val root = Json.parseToJsonElement(text)
            val groupElement = root.jsonObject[group]
                ?: root.jsonObject.entries.firstOrNull { it.key.trim().equals(group.trim(), ignoreCase = true) }?.value
                ?: root.jsonObject.values.firstOrNull()
                ?: return emptyList()

            val groupData = groupElement.jsonArray.getOrNull(1)?.jsonObject?.values?.toList() ?: return emptyList()
            val weeksToParse = if (groupData.size > 1) groupData.take(2) else groupData

            weeksToParse.flatMap { weekElem ->
                val weekArray = weekElem.jsonArray.toList()
                if (weekArray.size > 1) {
                    parseWeek(weekArray[1])
                } else {
                    emptyList()
                }
            }.sortedBy { it.date }
        } catch (e: Exception) {
            log?.invoke("Ошибка загрузки расписания ИУБиП для $group: ${e.message}")
            emptyList()
        }
    }

    private fun parseWeek(week: JsonElement): List<DayScheduleDto> {
        val days = mutableListOf<DayScheduleDto>()

        week.jsonObject.entries.forEach { (_, dayScheduleRaw) ->
            var dayDate: LocalDate? = null

            for ((_, otUnitsRaw) in dayScheduleRaw.jsonObject.entries) {
                val firstUnit = otUnitsRaw.jsonArray.firstOrNull()?.jsonObject ?: continue
                val rawDate = firstUnit["DATE"]?.jsonPrimitive?.content?.trim() ?: continue
                if (rawDate.isNotEmpty()) {
                    try {
                        val raw = rawDate.split(Regex("[-./]")).mapNotNull { it.trim().toIntOrNull() }
                        if (raw.size >= 3) {
                            dayDate = if (raw[0] > 1000) LocalDate.of(raw[0], raw[1], raw[2])
                            else LocalDate.of(raw[2], raw[1], raw[0])
                            break
                        }
                    } catch (_: Exception) {
                    }
                }
            }

            val validDate = dayDate ?: return@forEach

            val lessons = mutableListOf<LessonDto>()
            dayScheduleRaw.jsonObject.entries.forEach { (lessonNumRaw, otUnitsRaw) ->
                val number = lessonNumRaw.trim().toIntOrNull() ?: return@forEach
                val time = IUBIPLessonsSchedule.COMMON.firstOrNull { it.number == number } ?: return@forEach
                val unitsArray = otUnitsRaw.jsonArray
                if (unitsArray.isEmpty()) return@forEach

                val otUnits = unitsArray.map {
                    val obj = it.jsonObject
                    val auditory = obj["AUD"]?.jsonPrimitive?.content?.trim() ?: ""
                    val groupName = obj["GROUP"]?.jsonPrimitive?.content?.trim() ?: ""
                    val teacherName = obj["NAME"]?.jsonPrimitive?.content?.trim() ?: ""
                    if (teacherName.isNotBlank() && teacherName.length > 2) {
                        cachedTeachers.add(teacherName)
                    }

                    OneTimeUnitDto(
                        teacher = teacherName,
                        group = groupName,
                        room = auditory,
                        additional = if ("Дис" in auditory) "*" else "1"
                    )
                }

                val firstObj = unitsArray[0].jsonObject
                fun getField(key: String) = firstObj[key]?.jsonPrimitive?.content?.trim() ?: ""
                val isDeleted = getField("deleted").toIntOrNull() == 1

                lessons.add(
                    LessonDto(
                        date = validDate,
                        number = number,
                        startTime = time.start,
                        endTime = time.end,
                        subject = getField("SUBJECT"),
                        type = when (getField("SUBJ_TYPE")) {
                            "Урок" -> LessonTypeDto.COMMON
                            else -> LessonTypeDto.LECTURE
                        },
                        state = if (isDeleted) LessonStateDto.REMOVED else LessonStateDto.COMMON,
                        otUnits = otUnits
                    )
                )
            }

            if (lessons.isNotEmpty()) {
                days.add(
                    DayScheduleDto(
                        date = validDate,
                        scheduleType = LessonsScheduleTypeDto.COMMON,
                        lessons = lessons
                    )
                )
            }
        }

        return days
    }
}

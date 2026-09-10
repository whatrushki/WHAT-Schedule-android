package app.what.schedule.rinh

import app.what.schedule.core.clients.ScheduleClient
import app.what.schedule.core.models.*
import app.what.schedule.rinh.models.RINHApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.encodeURLPathPart
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class RINHScheduleClient(
    private val client: HttpClient,
    private val baseUrl: String = "https://rasp-api.rsue.ru/api",
    private val log: ((String) -> Unit)? = null
) : ScheduleClient {

    private var cachedGroupsAndTeachers: List<RINHApi.Schedule.Responses.ScheduleSearch>? = null

    private suspend fun getGroupsAndTeachers(): List<RINHApi.Schedule.Responses.ScheduleSearch> {
        cachedGroupsAndTeachers?.let { return it }
        return try {
            val response = client
                .get("$baseUrl/v1/schedule/search?format=json")
                .body<List<RINHApi.Schedule.Responses.ScheduleSearch>>()
            cachedGroupsAndTeachers = response
            response
        } catch (e: Exception) {
            log?.invoke("Ошибка загрузки поиска РГЭУ РИНХ: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getGroups(): List<GroupDto> {
        return getGroupsAndTeachers()
            .filter { "," !in it.name && "." !in it.name && "№" !in it.name && it.name.isNotBlank() }
            .map { GroupDto(id = it.name.trim(), name = it.name.trim(), course = 1) }
            .distinctBy { it.name }
            .sortedBy { it.name }
    }

    override suspend fun getTeachers(): List<TeacherDto> {
        return getGroupsAndTeachers()
            .filter { ("," in it.name || "." in it.name || "№" in it.name) && it.name.isNotBlank() }
            .map { TeacherDto(id = it.name.trim(), name = it.name.trim()) }
            .distinctBy { it.name }
            .sortedBy { it.name }
    }

    override suspend fun getGroupSchedule(
        group: String,
        showReplacements: Boolean
    ): List<DayScheduleDto> = fetchSchedule(group)

    override suspend fun getTeacherSchedule(
        teacher: String,
        showReplacements: Boolean
    ): List<DayScheduleDto> = fetchSchedule(teacher)

    private suspend fun fetchSchedule(value: String): List<DayScheduleDto> {
        return try {
            val encodedValue = value.encodeURLPathPart()
            val schedule = client
                .get("$baseUrl/v1/schedule/lessons/$encodedValue?format=json")
                .body<RINHApi.Schedule.Responses.GetSchedule>()
            schedule.toDaySchedules()
        } catch (e: Exception) {
            log?.invoke("Ошибка загрузки расписания РГЭУ РИНХ для $value: ${e.message}")
            emptyList()
        }
    }

    private fun RINHApi.Schedule.Responses.GetSchedule.toDaySchedules(): List<DayScheduleDto> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        return weeks.flatMap { week ->
            week.days.filter { day ->
                try {
                    val d = day.date.toLocalDate()
                    d >= now && day.pairs.any { it.lessons.isNotEmpty() }
                } catch (_: Exception) {
                    false
                }
            }
        }.mapNotNull { day ->
            try {
                val date = day.date.toLocalDate()
                DayScheduleDto(
                    date = date,
                    scheduleType = LessonsScheduleTypeDto.COMMON,
                    lessons = day.pairs.filterNot { it.lessons.isEmpty() }
                        .mapNotNull { it.toLessonDto(date) }
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun RINHApi.Schedule.Responses.APair.toLessonDto(date: LocalDate): LessonDto? {
        val firstLesson = lessons.firstOrNull() ?: return null
        return LessonDto(
            date = date,
            number = id,
            startTime = startTime.toLocalTime(),
            endTime = endTime.toLocalTime(),
            subject = "${firstLesson.kind.shortName} ${firstLesson.subject}".trim(),
            type = when (firstLesson.kind.id) {
                1 -> LessonTypeDto.LECTURE
                2 -> LessonTypeDto.PRACTICE
                3 -> LessonTypeDto.LABORATORY
                5 -> LessonTypeDto.CREDIT
                else -> LessonTypeDto.COMMON
            },
            state = LessonStateDto.COMMON,
            otUnits = lessons.map {
                val aud = if (it.audience.isNotEmpty() && (it.audience[0].isDigit() || it.audience[0] == 'с')) {
                    it.audience
                } else if (it.audience.isNotEmpty()) {
                    it.audience.drop(1)
                } else ""

                val building = when (it.audience.firstOrNull()) {
                    '*' -> "2"
                    '#' -> "3"
                    '&' -> "4"
                    'д' -> "д"
                    else -> "1"
                }

                OneTimeUnitDto(
                    teacher = it.teacher.name,
                    group = it.group,
                    room = aud,
                    additional = building
                )
            }
        )
    }

    private fun String.toLocalTime(): LocalTime {
        val parts = split(":").mapNotNull { it.trim().toIntOrNull() }
        return if (parts.size >= 2) {
            LocalTime(parts[0], parts[1], parts.getOrNull(2) ?: 0)
        } else {
            LocalTime(0, 0)
        }
    }

    private fun String.toLocalDate(): LocalDate {
        val parts = split(".", "-", "/").mapNotNull { it.trim().toIntOrNull() }
        return if (parts.size >= 3) {
            if (parts[0] > 1000) LocalDate(parts[0], parts[1], parts[2])
            else LocalDate(parts[2], parts[1], parts[0])
        } else {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
    }
}

package app.what.schedule.data.remote.providers.rinh.services

import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.asyncLazy
import app.what.schedule.data.remote.api.AdditionalData
import app.what.schedule.data.remote.api.ScheduleResponse
import app.what.schedule.data.remote.api.ScheduleService
import app.what.schedule.data.remote.api.models.DaySchedule
import app.what.schedule.data.remote.api.models.Group
import app.what.schedule.data.remote.api.models.Lesson
import app.what.schedule.data.remote.api.models.LessonState
import app.what.schedule.data.remote.api.models.LessonType
import app.what.schedule.data.remote.api.models.LessonsScheduleType
import app.what.schedule.data.remote.api.models.OneTimeUnit
import app.what.schedule.data.remote.api.models.Teacher
import app.what.schedule.data.remote.providers.rinh.RINHApi
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class RINHScheduleService(
    private val baseUrl: String,
    private val client: HttpClient,
    scope: CoroutineScope
) : ScheduleService {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private val getGroupsAndTeachers by scope.asyncLazy { getGroupsAndTeachers() }

    private suspend fun getGroupsAndTeachers() = client
        .get("$baseUrl/v1/schedule/search?format=json")
        .body<List<RINHApi.Schedule.Responses.ScheduleSearch>>()

    private suspend fun getSchedule(value: String): ScheduleResponse {
        val scheduleTag = buildTag(LogScope.SCHEDULE, LogCat.NET, "rinh")
        Auditor.debug(scheduleTag, "Запрос расписания: $value")
        crashlytics.setCustomKey("schedule_value", value)
        crashlytics.setCustomKey("institution", "rinh")

        val encodedValue = withContext(IO) {
            URLEncoder.encode(value, "UTF-8").replace("+", "%20")
        }

        val schedules = client
            .get("$baseUrl/v1/schedule/lessons/$encodedValue?format=json")
            .body<RINHApi.Schedule.Responses.GetSchedule>()
            .toDaySchedules()
            .takeIf(List<DaySchedule>::isNotEmpty)
            ?.let { ScheduleResponse.Available.FromSource(it, LocalDateTime.now()) }
            ?: ScheduleResponse.Empty

        Auditor.debug(
            scheduleTag,
            "Получено дней в расписании: ${if (schedules is ScheduleResponse.Available) schedules.schedules.size else 0}"
        )
        return schedules
    }


    override suspend fun getGroupSchedule(
        group: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): ScheduleResponse = getSchedule(group)

    override suspend fun getTeacherSchedule(
        teacher: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): ScheduleResponse = getSchedule(teacher)

    override suspend fun getGroups(): List<Group> {
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "rinh")
        Auditor.debug(netTag, "Загрузка списка групп")

        val groups = getGroupsAndTeachers
            .await()
            .filter { "," !in it.name && "." !in it.name && "№" !in it.name }
            .map { Group(it.name.trim()) }

        Auditor.debug(netTag, "Загружено групп: ${groups.size}")
        return groups
    }

    override suspend fun getTeachers(): List<Teacher> {
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "rinh")
        Auditor.debug(netTag, "Загрузка списка преподавателей")

        val teachers = getGroupsAndTeachers
            .await()
            .filter { "," in it.name || "." in it.name || "№" in it.name }
            .map { Teacher(it.name) }

        Auditor.debug(netTag, "Загружено преподавателей: ${teachers.size}")
        return teachers
    }

    private fun RINHApi.Schedule.Responses.GetSchedule.toDaySchedules(): List<DaySchedule> {
        val now = LocalDate.now()

        return weeks.map {
            it.days.filter { it.date.toLocalDate() >= now && it.pairs.any { it.lessons.isNotEmpty() } }
        }.flatten().map {
            DaySchedule(
                date = it.date.toLocalDate(),
                scheduleType = LessonsScheduleType.COMMON,
                lessons = it.pairs.filterNot { it.lessons.isEmpty() }
                    .map { raw -> raw.toLesson(it.date.toLocalDate()) }
            )
        }
    }

    private fun RINHApi.Schedule.Responses.APair.toLesson(date: LocalDate): Lesson = Lesson(
        date = date,
        number = id,
        startTime = startTime.toLocalTime(),
        endTime = endTime.toLocalTime(),
        subject = "${lessons.first().kind.shortName} ${lessons.first().subject}",
        type = when (lessons.first().kind.id) {
            1 -> LessonType.LECTURE
            2 -> LessonType.PRACTISE
            3 -> LessonType.LABORATORY
            5 -> LessonType.CREDIT
            else -> LessonType.COMMON
        },
        state = LessonState.COMMON,
        otUnits = lessons.map {
            OneTimeUnit(
                group = Group(it.group),
                teacher = Teacher(it.teacher.name),
                auditory = it.audience.let { if (it[0].isDigit() || it[0] == 'с') it else it.drop(1) },
                building = when (it.audience[0]) {
                    '*' -> "2"
                    '#' -> "3"
                    '&' -> "4"
                    'д' -> "д"
                    else -> "1"
                }
            )
        }
    )
}

private fun String.toLocalTime() =
    split(":").let { LocalTime.of(it[0].toInt(), it[1].toInt(), it[2].toInt()) }

private fun String.toLocalDate() =
    split(".").let { LocalDate.of(it[2].toInt(), it[1].toInt(), it[0].toInt()) }
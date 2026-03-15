package app.what.schedule.data.remote.providers.dgtu.services

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
import app.what.schedule.data.remote.api.sum
import app.what.schedule.data.remote.providers.dgtu.ApiResponse
import app.what.schedule.data.remote.providers.dgtu.DGTUApi
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DGTUScheduleService(
    private val baseUrl: String,
    private val client: HttpClient,
    private val scope: CoroutineScope
) : ScheduleService {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private val listYears by scope.asyncLazy { listYears() }

    private suspend fun listYears() = client
        .get("$baseUrl/Rasp/ListYears")
        .body<ApiResponse<DGTUApi.Schedule.ListYears>>()
        .data.years

    override suspend fun getGroupSchedule(
        group: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): ScheduleResponse = getSchedule(false, group, showReplacements, additional)

    override suspend fun getTeacherSchedule(
        teacher: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): ScheduleResponse = getSchedule(true, teacher, showReplacements, additional)

    private suspend fun getSchedule(
        isTeacher: Boolean, value: String, showReplacements: Boolean, additional: AdditionalData
    ): ScheduleResponse = coroutineScope {
        val scheduleTag = buildTag(LogScope.SCHEDULE, LogCat.NET, "dgtu")
        Auditor.debug(
            scheduleTag,
            "Запрос расписания ${if (isTeacher) "преподавателя" else "группы"}: $value"
        )
        crashlytics.setCustomKey(if (isTeacher) "schedule_teacher" else "schedule_group", value)
        crashlytics.setCustomKey("institution", "dgtu")

        val responses = mutableListOf<Deferred<ScheduleResponse>>()
        listOf(LocalDate.now(), LocalDate.now().plusWeeks(1)).forEach {
            val job = async {
                client.get(
                    "$baseUrl/Rasp?id${if (isTeacher) "Teacher" else "Group"}=$value&sdate=${
                        it.format(DateTimeFormatter.ISO_DATE)
                    }"
                ).body<ApiResponse<DGTUApi.Schedule.Get>>().data.rasp.toDaySchedules()
            }

            responses.add(job)
        }

        val result = responses.awaitAll().sum()

        Auditor.debug(
            scheduleTag,
            "Получено дней в расписании: ${if (result is ScheduleResponse.Available) result.schedules.size else 0}"
        )

        return@coroutineScope result
    }

    override suspend fun getGroups(): List<Group> {
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "dgtu")
        Auditor.debug(netTag, "Загрузка списка групп")

        val year = listYears.await().last()
        Auditor.debug(netTag, "Используемый год: $year")

        val groups = client
            .get("$baseUrl/raspGrouplist?year=$year")
            .body<ApiResponse<List<DGTUApi.Models.DGTUGroup>>>()
            .data.map { Group(it.name, it.id.toString(), it.kurs) }
            .sortedBy { it.name }

        Auditor.debug(netTag, "Загружено групп: ${groups.size}")
        return groups
    }

    override suspend fun getTeachers(): List<Teacher> {
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "dgtu")
        Auditor.debug(netTag, "Загрузка списка преподавателей")

        val year = listYears.await().last()
        Auditor.debug(netTag, "Используемый год: $year")

        val teachers = client
            .get("$baseUrl/raspTeacherlist?year=$year")
            .body<ApiResponse<List<DGTUApi.Models.DGTUTeacher>>>()
            .data.map {
                Teacher(it.name.split(" ").let {
                    it[0] + " " + it.mapIndexedNotNull { index, s ->
                        if (index in 1..2 && s.isNotEmpty()) "${s[0]}." else null
                    }.joinToString("")
                }, it.id.toString())
            }
            .sortedBy { it.name }

        Auditor.debug(netTag, "Загружено преподавателей: ${teachers.size}")
        return teachers
    }


    private fun List<DGTUApi.Models.DGTULesson>.toDaySchedules() =
        map { it.toLesson(it.date.toLocalDate()) }.groupBy { it.date }.map { (day, lessons) ->
            DaySchedule(
                day, LessonsScheduleType.COMMON,
                lessons.groupBy { it.number }.map { (_, less) ->
                    less[0] + less.slice(1 until less.size)
                }
            )
        }.takeIf(List<DaySchedule>::isNotEmpty)
            ?.let { ScheduleResponse.Available.FromSource(it, LocalDateTime.now()) }
            ?: ScheduleResponse.Empty

    private fun DGTUApi.Models.DGTULesson.toLesson(date: LocalDate): Lesson {
        val rawData = this.auditory.split("-")
        val (building, auditory) = try {
            rawData[0] to rawData[1]
        } catch (_: Exception) {
            "-" to rawData.joinToString("")
        }

        return Lesson(
            date = date,
            number = this.number,
            startTime = this.startTime.toLocalTime(),
            endTime = endTime.toLocalTime(),
            subject = subject,
            type = if (this.number == 0) LessonType.OBLIGATION else LessonType.COMMON,
            state = if (replacement == true) LessonState.CHANGED
            else LessonState.COMMON,
            otUnits = group.split(",").map {
                OneTimeUnit(
                    group = Group(it.trim(), codeGroup.toString()),
                    teacher = Teacher(teacher, codeTeacher.toString()),
                    building = building,
                    auditory = auditory
                )
            }
        )
    }
}
package app.what.schedule.dgtu

import app.what.schedule.core.clients.ScheduleClient
import app.what.schedule.core.models.*
import app.what.schedule.dgtu.models.ApiResponse
import app.what.schedule.dgtu.models.DGTUApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DGTUScheduleClient(
    private val client: HttpClient,
    private val baseUrl: String = "https://edu.donstu.ru/api",
    private val log: ((String) -> Unit)? = null
) : ScheduleClient {

    private var cachedYears: List<String>? = null

    private suspend fun getYears(): List<String> {
        cachedYears?.let { return it }
        val response = client.get("$baseUrl/Rasp/ListYears")
            .body<ApiResponse<DGTUApi.Schedule.ListYears>>()
            .data.years
        cachedYears = response
        return response
    }

    override suspend fun getGroups(): List<GroupDto> {
        val year = getYears().lastOrNull() ?: "2025-2026"
        return client.get("$baseUrl/raspGrouplist?year=$year")
            .body<ApiResponse<List<DGTUApi.Models.DGTUGroup>>>()
            .data.map { GroupDto(id = it.id.toString(), name = it.name.trim(), course = it.kurs ?: 1) }
            .distinctBy { it.name }
            .sortedBy { it.name }
    }

    override suspend fun getTeachers(): List<TeacherDto> {
        val year = getYears().lastOrNull() ?: "2025-2026"
        return client.get("$baseUrl/raspTeacherlist?year=$year")
            .body<ApiResponse<List<DGTUApi.Models.DGTUTeacher>>>()
            .data.map { teacher ->
                val parts = teacher.name.split(" ")
                val formattedName = if (parts.size >= 3) {
                    "${parts[0]} ${parts[1].firstOrNull() ?: ""}.${parts[2].firstOrNull() ?: ""}."
                } else teacher.name
                TeacherDto(id = teacher.id.toString(), name = formattedName.trim())
            }
            .distinctBy { it.name }
            .sortedBy { it.name }
    }

    override suspend fun getGroupSchedule(group: String, showReplacements: Boolean): List<DayScheduleDto> =
        fetchSchedule(targetId = group, isTeacher = false)

    override suspend fun getTeacherSchedule(teacher: String, showReplacements: Boolean): List<DayScheduleDto> =
        fetchSchedule(targetId = teacher, isTeacher = true)

    private suspend fun fetchSchedule(targetId: String, isTeacher: Boolean): List<DayScheduleDto> = coroutineScope {
        log?.invoke("Запрос расписания ДГТУ для ${if (isTeacher) "преподавателя" else "группы"}: $targetId")

        val paramName = if (isTeacher) "idTeacher" else "idGroup"
        
        try {
            // Запрос полного расписания без привязки к дате (возвращает все занятия семестра)
            val fullUrl = "$baseUrl/Rasp?$paramName=$targetId"
            val response = client.get(fullUrl).body<ApiResponse<DGTUApi.Schedule.Get>>()
            val schedules = response.data.rasp.toDaySchedules()
            if (schedules.isNotEmpty()) {
                return@coroutineScope schedules
            }
        } catch (e: Exception) {
            log?.invoke("Ошибка загрузки общего расписания: ${e.message}")
        }

        // Запасной вариант: запрос по неделям
        val weeks = listOf(LocalDate.now(), LocalDate.now().plusWeeks(1))
        val jobs = weeks.map { weekDate ->
            async {
                try {
                    val dateFormatted = weekDate.format(DateTimeFormatter.ISO_DATE)
                    val url = "$baseUrl/Rasp?$paramName=$targetId&sdate=$dateFormatted"
                    val response = client.get(url).body<ApiResponse<DGTUApi.Schedule.Get>>()
                    response.data.rasp.toDaySchedules()
                } catch (e: Exception) {
                    log?.invoke("Ошибка загрузки недели $weekDate: ${e.message}")
                    emptyList()
                }
            }
        }

        val allWeeks = jobs.awaitAll().flatten()
        allWeeks.groupBy { it.date }.map { (date, days) ->
            val allLessons = days.flatMap { it.lessons }.distinctBy { "${it.number}_${it.subject}_${it.startTime}" }
            DayScheduleDto(
                date = date,
                scheduleType = LessonsScheduleTypeDto.COMMON,
                lessons = allLessons
            )
        }.sortedBy { it.date }
    }

    private fun List<DGTUApi.Models.DGTULesson>.toDaySchedules(): List<DayScheduleDto> =
        map { it.toLessonDto() }
            .groupBy { it.date }
            .map { (day, lessons) ->
                val mergedLessons = lessons.groupBy { it.number }.map { (_, lessonGroup) ->
                    lessonGroup.reduce { acc, next -> acc + next }
                }
                DayScheduleDto(
                    date = day,
                    scheduleType = LessonsScheduleTypeDto.COMMON,
                    lessons = mergedLessons.sortedBy { it.number }
                )
            }.sortedBy { it.date }

    private fun DGTUApi.Models.DGTULesson.toLessonDto(): LessonDto {
        val rawData = auditory?.split("-") ?: emptyList()
        val building = if (rawData.size > 1) "Корпус ${rawData[0]}" else "Главный"
        val aud = if (rawData.size > 1) rawData[1] else (auditory ?: "")

        val otUnit = OneTimeUnitDto(
            group = group ?: "",
            teacher = teacherName ?: teacher ?: "",
            room = aud,
            additional = building
        )

        return LessonDto(
            date = date.toLocalDate(),
            number = number,
            startTime = startTime.toLocalTime(),
            endTime = endTime.toLocalTime(),
            subject = subject,
            otUnits = listOf(otUnit),
            type = if (number == 0) LessonTypeDto.UNKNOWN else LessonTypeDto.COMMON,
            state = if (replacement == true) LessonStateDto.CHANGED else LessonStateDto.COMMON
        )
    }
}

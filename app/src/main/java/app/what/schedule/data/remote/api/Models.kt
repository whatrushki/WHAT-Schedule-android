package app.what.schedule.data.remote.api

import java.time.LocalDate
import java.time.LocalTime

data class DaySchedule(
    val date: LocalDate,
    val dateDescription: String,
    val lessons: List<Lesson>
)

data class Lesson(
    val subject: String,
    val group: String,
    val teacher: String,
    val auditory: String,
    val startDate: LocalTime,
    val endDate: LocalTime
)

enum class ParseMode {
    TEACHER,
    GROUP
}
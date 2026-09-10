package app.what.schedule.core.models

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

@Serializable
data class InstitutionMetaDto(
    val id: String,
    val name: String,
    val fullName: String,
    val description: String,
    val sourceTypes: Set<SourceTypeDto>,
    val sourceUrl: String,
    val hasAccountService: Boolean = false
)

enum class SourceTypeDto { API, PARSER, EXCEL, PDF }

data class DayScheduleDto(
    val date: LocalDate,
    val scheduleType: LessonsScheduleTypeDto,
    val lessons: List<LessonDto>
)

data class LessonDto(
    val date: LocalDate,
    val number: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val subject: String,
    val otUnits: List<OneTimeUnitDto>,
    val type: LessonTypeDto,
    val state: LessonStateDto = LessonStateDto.COMMON
) {
    infix operator fun plus(other: LessonDto) = copy(otUnits = otUnits + other.otUnits)
    infix operator fun plus(other: List<LessonDto>) = copy(otUnits = otUnits + other.flatMap(LessonDto::otUnits))
    fun equalsWithReplacement(other: LessonDto): Boolean = otUnits.toSet() == other.otUnits.toSet()
}

enum class LessonsScheduleTypeDto {
    COMMON,
    SHORTENED,
    WITH_CLASS_HOUR
}

enum class LessonStateDto {
    COMMON, ADDED, REMOVED, CHANGED
}

data class OneTimeUnitDto(
    val teacher: String,
    val group: String,
    val room: String,
    val additional: String = ""
)

enum class LessonTypeDto {
    COMMON,
    PRACTICE,
    LECTURE,
    LABORATORY,
    EXAM,
    CREDIT,
    CONSULTATION,
    OTHER,
    UNKNOWN;

    companion object {
        fun fromString(value: String): LessonTypeDto = when {
            value.contains("пр", ignoreCase = true) -> PRACTICE
            value.contains("лек", ignoreCase = true) -> LECTURE
            value.contains("лаб", ignoreCase = true) -> LABORATORY
            value.contains("экз", ignoreCase = true) -> EXAM
            value.contains("зач", ignoreCase = true) -> CREDIT
            value.contains("конс", ignoreCase = true) -> CONSULTATION
            else -> OTHER
        }
    }
}

data class LessonTimeDto(
    val number: Int,
    val start: LocalTime,
    val end: LocalTime
)

data class GroupDto(
    val id: String,
    val name: String,
    val course: Int = 0
)

data class TeacherDto(
    val id: String,
    val name: String
)

sealed interface SearchTargetDto {
    val id: String
    val name: String

    data class Group(override val id: String, override val name: String) : SearchTargetDto
    data class Teacher(override val id: String, override val name: String) : SearchTargetDto
}

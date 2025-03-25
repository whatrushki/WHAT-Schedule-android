package app.what.schedule.data.remote.api

import java.time.LocalDate
import java.time.LocalTime

// -- Расписание пар

data class DaySchedule(
    val date: LocalDate,
    val dateDescription: String,
    val scheduleType: LessonsScheduleType,
    val lessons: List<Lesson>
)

data class Lesson(
    val number: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val subject: String,
    val otUnits: List<OneTimeUnit>,
    val type: LessonType,
    val state: LessonState = LessonState.COMMON
) {
    operator fun plus(other: Lesson) = copy(otUnits = otUnits + other.otUnits)

    fun equalsWithReplacement(other: Lesson): Boolean {
        return otUnits.toSet() == other.otUnits.toSet()
    }
}

enum class LessonsScheduleType {
    COMMON,
    SHORTENED,
    WITH_CLASS_HOUR
}

enum class LessonState {
    COMMON, ADDED, REMOVED, CHANGED;

    val isCommon get() = this == COMMON
    val isAdded get() = this == ADDED
    val isRemoved get() = this == REMOVED
    val isChanged get() = this == CHANGED
}

enum class LessonType {
    COMMON, ADDITIONAL, CLASS_HOUR, LECTURE, PRACTISE, LABORATORY, CREDIT;

    val isStandard get() = this != ADDITIONAL && this != CLASS_HOUR && this != LABORATORY && this != CREDIT
    val isNonStandard get() = !isStandard
}

data class OneTimeUnit(
    val group: Group,
    val teacher: Teacher,
    val auditory: String,
    val building: String,
) {
    companion object {
        fun empty() = OneTimeUnit(Group("-"), Teacher("-"), "-", "-")
    }
}

data class Group(
    val name: String,
    val id: String = name,
    val year: Int? = null
)

data class Teacher(
    val name: String,
    val id: String = name
)

enum class ParseMode {
    TEACHER,
    GROUP
}

// -- Расписание звонков

data class LessonTime(
    val number: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val type: LessonType = LessonType.COMMON
)

interface LessonsSchedule {
    val COMMON: List<LessonTime>
    val SHORTENED: List<LessonTime>
    val WITH_CLASS_HOUR: List<LessonTime>

    fun List<LessonTime>.numberOf(time: LocalTime) = firstOrNull { it.startTime == time }?.number

    fun List<LessonTime>.getByNumber(number: Int) = firstOrNull { it.number == number }
}

sealed class ScheduleSearch(val name: String, val id: String) {
    class Group(name: String, id: String = name) : ScheduleSearch(name, id)
    class Teacher(name: String, id: String = name) : ScheduleSearch(name, id)
}

fun Group.toScheduleSearch() = ScheduleSearch.Group(name, id)
fun Teacher.toScheduleSearch() = ScheduleSearch.Teacher(name, id)

fun ScheduleSearch.Group.toGroup() = Group(name, id)
fun ScheduleSearch.Teacher.toTeacher() = Teacher(name, id)
package app.what.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class DaySchedule(
    val date: LocalDate,
    val scheduleType: LessonsScheduleType,
    val lessons: List<Lesson>
)

data class Lesson(
    val date: LocalDate,
    val number: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val subject: String,
    val otUnits: List<OneTimeUnit>,
    val type: LessonType,
    val state: LessonState = LessonState.COMMON
) {
    infix operator fun plus(other: Lesson) = copy(otUnits = otUnits + other.otUnits)
    infix operator fun plus(other: List<Lesson>) =
        copy(otUnits = otUnits + other.flatMap(Lesson::otUnits))
    
    fun equalsWithReplacement(other: Lesson): Boolean {
        if (otUnits.isEmpty() && other.otUnits.isEmpty()) return true
        if (otUnits.isEmpty() || other.otUnits.isEmpty()) return false

        fun clean(s: String) = s.replace(" ", "")
            .replace(".", "")
            .replace("—", "-")
            .replace("–", "-")
            .replace("с", "c", ignoreCase = true)
            .replace("а", "a", ignoreCase = true)
            .replace("о", "o", ignoreCase = true)
            .replace("р", "p", ignoreCase = true)
            .replace("х", "x", ignoreCase = true)
            .replace("е", "e", ignoreCase = true)
            .trim()
            .lowercase()

        fun cleanRoom(r: String): String {
            val trimmed = r.trim().removeSuffix(".0")
            return clean(trimmed)
        }

        return otUnits.all { unit ->
            other.otUnits.any { otherUnit ->
                clean(unit.teacher.name) == clean(otherUnit.teacher.name) &&
                cleanRoom(unit.auditory) == cleanRoom(otherUnit.auditory)
            }
        }
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
    COMMON, ADDITIONAL, CLASS_HOUR, LECTURE, PRACTISE, LABORATORY, CREDIT, OBLIGATION;
    
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

@Serializable
data class Group(
    val name: String,
    val id: String = name,
    val year: Int? = null,
    val favorite: Boolean = false
)

@Serializable
data class Teacher(
    val name: String,
    val id: String = name,
    val favorite: Boolean = false
)

enum class ParseMode {
    TEACHER,
    GROUP
}

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

@Serializable
sealed class ScheduleSearch {
    abstract val name: String
    abstract val id: String
    abstract val favorite: Boolean
    
    @Serializable
    @SerialName("group")
    class Group(
        override val name: String,
        override val id: String = name,
        override val favorite: Boolean = false
    ) : ScheduleSearch()
    
    @Serializable
    @SerialName("teacher")
    class Teacher(
        override val name: String,
        override val id: String = name,
        override val favorite: Boolean = false
    ) : ScheduleSearch()
    
    operator fun component1() = name
    operator fun component2() = id
    operator fun component3() = favorite
    
    override fun equals(other: Any?): Boolean =
        other is ScheduleSearch && this::class == other::class && id == other.id
    
    override fun hashCode(): Int {
        var result = favorite.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
    
    fun copy(
        name: String = this.name,
        id: String = this.id,
        favorite: Boolean = this.favorite
    ) = when (this) {
        is Group -> Group(name, id, favorite)
        is Teacher -> Teacher(name, id, favorite)
    }
}

fun Group.toScheduleSearch() = ScheduleSearch.Group(name, id, favorite)
fun Teacher.toScheduleSearch() = ScheduleSearch.Teacher(name, id, favorite)

fun ScheduleSearch.Group.toGroup() = Group(name, id, favorite = favorite)
fun ScheduleSearch.Teacher.toTeacher() = Teacher(name, id, favorite)

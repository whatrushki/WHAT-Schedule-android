package app.what.data.mappers

import androidx.compose.ui.text.AnnotatedString
import app.what.domain.models.*
import app.what.schedule.core.models.*
import java.time.LocalDateTime

fun GroupDto.toDomain(): Group = Group(name = name, id = id)
fun TeacherDto.toDomain(): Teacher = Teacher(name = name, id = id)

fun OneTimeUnitDto.toDomain(): OneTimeUnit = OneTimeUnit(
    group = Group(name = group, id = group),
    teacher = Teacher(name = teacher, id = teacher),
    auditory = room,
    building = additional
)

fun LessonTypeDto.toDomain(): LessonType = when (this) {
    LessonTypeDto.LECTURE -> LessonType.LECTURE
    LessonTypeDto.PRACTICE -> LessonType.PRACTISE
    LessonTypeDto.LABORATORY -> LessonType.LABORATORY
    LessonTypeDto.CREDIT, LessonTypeDto.EXAM -> LessonType.CREDIT
    else -> LessonType.COMMON
}

fun LessonStateDto.toDomain(): LessonState = when (this) {
    LessonStateDto.ADDED -> LessonState.ADDED
    LessonStateDto.REMOVED -> LessonState.REMOVED
    LessonStateDto.CHANGED -> LessonState.CHANGED
    LessonStateDto.COMMON -> LessonState.COMMON
}

fun LessonsScheduleTypeDto.toDomain(): LessonsScheduleType = when (this) {
    LessonsScheduleTypeDto.SHORTENED -> LessonsScheduleType.SHORTENED
    LessonsScheduleTypeDto.WITH_CLASS_HOUR -> LessonsScheduleType.WITH_CLASS_HOUR
    LessonsScheduleTypeDto.COMMON -> LessonsScheduleType.COMMON
}

fun LessonDto.toDomain(): Lesson = Lesson(
    date = date,
    number = number,
    startTime = startTime,
    endTime = endTime,
    subject = subject,
    otUnits = otUnits.map { it.toDomain() },
    type = type.toDomain(),
    state = state.toDomain()
)

fun DayScheduleDto.toDomain(): DaySchedule = DaySchedule(
    date = date,
    scheduleType = scheduleType.toDomain(),
    lessons = lessons.map { it.toDomain() }
)

fun List<DayScheduleDto>.toDomainResponse(): ScheduleResponse {
    if (isEmpty()) return ScheduleResponse.Empty
    return ScheduleResponse.Available.FromSource(
        schedules = map { it.toDomain() },
        lastModified = LocalDateTime.now()
    )
}

fun NewListItemDto.toDomain(): NewListItem = NewListItem(
    id = id,
    url = sourceUrl ?: urlOrFallback(id),
    bannerUrl = imageUrl ?: "",
    title = title,
    description = description,
    timestamp = date,
    tags = emptyList()
)

fun NewDetailDto.toDomain(): NewItem = NewItem(
    id = id,
    url = sourceUrl ?: id,
    bannerUrl = images.firstOrNull(),
    title = title,
    description = null,
    tags = emptyList(),
    timestamp = date,
    content = NewContent.Item.Text(AnnotatedString(fullText))
)

private fun urlOrFallback(id: String): String = id

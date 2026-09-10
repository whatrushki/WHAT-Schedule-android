package app.what.data.repositories

import app.what.schedule.data.local.database.DayScheduleSDBO
import app.what.schedule.data.local.database.GroupDBO
import app.what.schedule.data.local.database.TeacherDBO
import app.what.domain.models.DaySchedule
import app.what.domain.models.Group
import app.what.domain.models.Lesson
import app.what.domain.models.OneTimeUnit
import app.what.domain.models.Teacher

fun TeacherDBO.toModel() = Teacher(
    name = name,
    id = teacherId,
    favorite = favorite
)

fun GroupDBO.toModel() = Group(
    name = name,
    id = groupId,
    favorite = favorite
)

fun DayScheduleSDBO.toModel() = DaySchedule(
    daySchedule.date,
    daySchedule.scheduleType,
    lessons.map {
        Lesson(
            date = daySchedule.date,
            number = it.lesson.number,
            subject = it.lesson.subject,
            type = it.lesson.type,
            startTime = it.lesson.startTime,
            endTime = it.lesson.endTime,
            state = it.lesson.state,
            otUnits = it.otUnits.map {
                OneTimeUnit(
                    group = it.group.toModel(),
                    teacher = it.teacher.toModel(),
                    building = it.unit.building,
                    auditory = it.unit.auditory
                )
            }
        )
    }
)

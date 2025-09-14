package app.what.schedule.domain

import app.what.schedule.data.local.database.DayScheduleSDBO
import app.what.schedule.data.local.database.GroupDBO
import app.what.schedule.data.local.database.TeacherDBO
import app.what.schedule.data.remote.api.DaySchedule
import app.what.schedule.data.remote.api.Group
import app.what.schedule.data.remote.api.Lesson
import app.what.schedule.data.remote.api.OneTimeUnit
import app.what.schedule.data.remote.api.Teacher

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
    "",
    daySchedule.scheduleType,
    lessons.map {
        Lesson(
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

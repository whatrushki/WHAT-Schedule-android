package app.what.schedule.data.remote.providers.iubip.general

import app.what.schedule.data.remote.api.models.LessonTime
import app.what.schedule.data.remote.api.models.LessonsSchedule
import java.time.LocalTime


object IUBIPLessonsSchedule : LessonsSchedule {
    override val COMMON = listOf(
        LessonTime(1, LocalTime.of(8, 20), LocalTime.of(9, 50)),
        LessonTime(2, LocalTime.of(10, 0), LocalTime.of(11, 30)),
        LessonTime(3, LocalTime.of(11, 40), LocalTime.of(13, 10)),
        LessonTime(4, LocalTime.of(13, 30), LocalTime.of(15, 0)),
        LessonTime(5, LocalTime.of(15, 10), LocalTime.of(16, 40)),
        LessonTime(6, LocalTime.of(17, 0), LocalTime.of(18, 30)),
        LessonTime(7, LocalTime.of(18, 40), LocalTime.of(20, 10)),
        LessonTime(8, LocalTime.of(20, 20), LocalTime.of(21, 50))
    )

    override val SHORTENED = emptyList<LessonTime>()

    override val WITH_CLASS_HOUR = emptyList<LessonTime>()
}
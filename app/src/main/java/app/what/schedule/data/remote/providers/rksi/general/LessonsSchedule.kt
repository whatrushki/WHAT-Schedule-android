package app.what.schedule.data.remote.providers.rksi.general

import app.what.schedule.data.remote.api.models.LessonTime
import app.what.schedule.data.remote.api.models.LessonType
import app.what.schedule.data.remote.api.models.LessonsSchedule
import java.time.LocalTime


object RKSILessonsSchedule : LessonsSchedule {
    override val COMMON = listOf(
        LessonTime(1, LocalTime.of(8, 0), LocalTime.of(9, 30)),
        LessonTime(2, LocalTime.of(9, 40), LocalTime.of(11, 10)),
        LessonTime(3, LocalTime.of(11, 30), LocalTime.of(13, 0)),
        LessonTime(4, LocalTime.of(13, 10), LocalTime.of(14, 40)),
        LessonTime(5, LocalTime.of(15, 0), LocalTime.of(16, 30)),
        LessonTime(6, LocalTime.of(16, 40), LocalTime.of(18, 10)),
        LessonTime(7, LocalTime.of(18, 20), LocalTime.of(19, 50))
    )

    override val SHORTENED = listOf(
        LessonTime(1, LocalTime.of(8, 0), LocalTime.of(8, 50)),
        LessonTime(2, LocalTime.of(9, 0), LocalTime.of(9, 50)),
        LessonTime(3, LocalTime.of(10, 0), LocalTime.of(10, 50)),
        LessonTime(4, LocalTime.of(11, 0), LocalTime.of(11, 50)),
        LessonTime(5, LocalTime.of(12, 0), LocalTime.of(12, 50)),
        LessonTime(6, LocalTime.of(13, 0), LocalTime.of(13, 50)),
        LessonTime(7, LocalTime.of(14, 0), LocalTime.of(14, 50))
    )

    override val WITH_CLASS_HOUR = listOf(
        LessonTime(1, LocalTime.of(8, 0), LocalTime.of(9, 30)),
        LessonTime(2, LocalTime.of(9, 40), LocalTime.of(11, 10)),
        LessonTime(3, LocalTime.of(11, 30), LocalTime.of(13, 0)),
        LessonTime(
            number = 0,
            LocalTime.of(13, 5),
            LocalTime.of(14, 5),
            type = LessonType.CLASS_HOUR
        ),
        LessonTime(4, LocalTime.of(14, 10), LocalTime.of(15, 40)),
        LessonTime(5, LocalTime.of(16, 0), LocalTime.of(17, 30)),
        LessonTime(6, LocalTime.of(17, 40), LocalTime.of(19, 10))
    )
}
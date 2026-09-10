package app.what.schedule.rksi

import app.what.schedule.core.models.*
import app.what.schedule.rksi.parser.RKSIReplacementsParser
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RKSIScheduleClientTest {

    @Test
    fun testCommonLessonsScheduleTimes() {
        val schedule = RKSILessonsSchedule.COMMON
        assertEquals(7, schedule.size)
        assertEquals(1, schedule.first().number)
        assertEquals("08:00", schedule.first().start.toString())
        assertEquals("09:30", schedule.first().end.toString())
        assertEquals("11:30", schedule[2].start.toString())
        assertEquals("13:00", schedule[2].end.toString())
        assertEquals("13:10", schedule[3].start.toString())
        assertEquals("14:40", schedule[3].end.toString())
        assertEquals(7, schedule.last().number)
        assertEquals("18:20", schedule.last().start.toString())
        assertEquals("19:50", schedule.last().end.toString())
    }

    @Test
    fun testShortenedLessonsScheduleTimes() {
        val schedule = RKSILessonsSchedule.SHORTENED
        assertEquals(7, schedule.size)
        assertEquals(1, schedule.first().number)
        assertEquals("08:00", schedule.first().start.toString())
        assertEquals("08:50", schedule.first().end.toString())
        assertEquals(7, schedule.last().number)
        assertEquals("14:00", schedule.last().start.toString())
        assertEquals("14:50", schedule.last().end.toString())
    }

    @Test
    fun testWithClassHourLessonsScheduleTimes() {
        val schedule = RKSILessonsSchedule.WITH_CLASS_HOUR
        assertEquals(7, schedule.size)
        assertEquals(1, schedule.first().number)
        assertEquals("08:00", schedule.first().start.toString())
        assertEquals("09:30", schedule.first().end.toString())
        val classHour = schedule.first { it.number == 0 }
        assertEquals("13:05", classHour.start.toString())
        assertEquals("14:05", classHour.end.toString())
        assertEquals(6, schedule.last().number)
        assertEquals("17:40", schedule.last().start.toString())
        assertEquals("19:10", schedule.last().end.toString())
    }

    @Test
    fun testLessonDtoEqualsWithReplacement() {
        val base = LessonDto(
            date = LocalDate(2026, 9, 10),
            number = 3,
            startTime = LocalTime(11, 30),
            endTime = LocalTime(13, 0),
            subject = "МДК 01.01",
            type = LessonTypeDto.COMMON,
            state = LessonStateDto.COMMON,
            otUnits = listOf(
                OneTimeUnitDto(
                    group = "ИС-43",
                    teacher = "Малая М.А.",
                    room = "422"
                )
            )
        )

        // Same teacher with spaces, room with .0, and latin letters
        val repIdentical = LessonDto(
            date = LocalDate(2026, 9, 10),
            number = 3,
            startTime = LocalTime(11, 30),
            endTime = LocalTime(13, 0),
            subject = "",
            type = LessonTypeDto.COMMON,
            state = LessonStateDto.CHANGED,
            otUnits = listOf(
                OneTimeUnitDto(
                    group = "ИС-43",
                    teacher = "Малая М. А.",
                    room = "422.0"
                )
            )
        )

        assertTrue(base.equalsWithReplacement(repIdentical))

        // Different room
        val repDiffRoom = repIdentical.copy(
            otUnits = listOf(
                OneTimeUnitDto(
                    group = "ИС-43",
                    teacher = "Малая М.А.",
                    room = "с/з2"
                )
            )
        )
        assertFalse(base.equalsWithReplacement(repDiffRoom))
    }

    @Test
    fun testApplyReplacementsIS43Scenario() {
        val date = LocalDate(2026, 9, 10)
        val baseLessons = listOf(
            LessonDto(
                date = date,
                number = 2,
                startTime = LocalTime(9, 40),
                endTime = LocalTime(11, 10),
                subject = "Физкультура",
                type = LessonTypeDto.COMMON,
                state = LessonStateDto.COMMON,
                otUnits = listOf(OneTimeUnitDto(group = "ИС-43", teacher = "Гузов А.В.", room = "с/з4"))
            ),
            LessonDto(
                date = date,
                number = 3,
                startTime = LocalTime(11, 30),
                endTime = LocalTime(13, 0),
                subject = "МДК 01.01",
                type = LessonTypeDto.COMMON,
                state = LessonStateDto.COMMON,
                otUnits = listOf(OneTimeUnitDto(group = "ИС-43", teacher = "Малая М.А.", room = "422"))
            ),
            LessonDto(
                date = date,
                number = 4,
                startTime = LocalTime(13, 10),
                endTime = LocalTime(14, 40),
                subject = "Экономика",
                type = LessonTypeDto.COMMON,
                state = LessonStateDto.COMMON,
                otUnits = listOf(OneTimeUnitDto(group = "ИС-43", teacher = "Павлова В.А.", room = "308"))
            )
        )

        val replacements = listOf(
            LessonDto(
                date = date,
                number = 1,
                startTime = LocalTime(0, 0),
                endTime = LocalTime(0, 0),
                subject = "",
                type = LessonTypeDto.COMMON,
                state = LessonStateDto.CHANGED,
                otUnits = listOf(OneTimeUnitDto(group = "ИС-43", teacher = "Бойнар И.Н.", room = "326"))
            ),
            LessonDto(
                date = date,
                number = 2,
                startTime = LocalTime(0, 0),
                endTime = LocalTime(0, 0),
                subject = "",
                type = LessonTypeDto.COMMON,
                state = LessonStateDto.CHANGED,
                otUnits = listOf(OneTimeUnitDto(group = "ИС-43", teacher = "Гузов А.В.", room = "с/з2"))
            ),
            LessonDto(
                date = date,
                number = 3,
                startTime = LocalTime(0, 0),
                endTime = LocalTime(0, 0),
                subject = "",
                type = LessonTypeDto.COMMON,
                state = LessonStateDto.CHANGED,
                otUnits = listOf(OneTimeUnitDto(group = "ИС-43", teacher = "Малая М. А.", room = "422.0"))
            )
        )

        val result = RKSIReplacementsParser.applyReplacements(
            baseLessons,
            replacements,
            RKSILessonsSchedule.COMMON
        )

        assertEquals(4, result.size)

        val p1 = result.first { it.number == 1 }
        assertEquals(LessonStateDto.ADDED, p1.state)
        assertEquals("Бойнар И.Н.", p1.otUnits.first().teacher)
        assertEquals("326", p1.otUnits.first().room)

        val p2 = result.first { it.number == 2 }
        assertEquals(LessonStateDto.CHANGED, p2.state)
        assertEquals("Гузов А.В.", p2.otUnits.first().teacher)
        assertEquals("с/з2", p2.otUnits.first().room)

        val p3 = result.first { it.number == 3 }
        assertEquals(LessonStateDto.COMMON, p3.state)
        assertEquals("Малая М.А.", p3.otUnits.first().teacher)
        assertEquals("422", p3.otUnits.first().room)

        val p4 = result.first { it.number == 4 }
        assertEquals(LessonStateDto.REMOVED, p4.state)
        assertEquals("Павлова В.А.", p4.otUnits.first().teacher)
    }
}
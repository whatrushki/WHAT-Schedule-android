package app.what.schedule.rksi

import kotlin.test.Test
import kotlin.test.assertEquals

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
}
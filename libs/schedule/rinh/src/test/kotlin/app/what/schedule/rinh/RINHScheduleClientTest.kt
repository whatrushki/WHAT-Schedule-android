package app.what.schedule.rinh

import app.what.schedule.rinh.models.RINHApi
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RINHScheduleClientTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun testParseScheduleSearch() {
        val jsonString = """
        [
            {"id": 1, "name": "ПИ-411"},
            {"id": 2, "name": "Иванов И.И."}
        ]
        """.trimIndent()

        val items = json.decodeFromString<List<RINHApi.Schedule.Responses.ScheduleSearch>>(jsonString)
        assertEquals(2, items.size)
        assertEquals("ПИ-411", items[0].name)
        assertEquals("Иванов И.И.", items[1].name)

        val groups = items.filter { "," !in it.name && "." !in it.name && "№" !in it.name && it.name.isNotBlank() }
        val teachers = items.filter { ("," in it.name || "." in it.name || "№" in it.name) && it.name.isNotBlank() }

        assertEquals(1, groups.size)
        assertEquals("ПИ-411", groups[0].name)
        assertEquals(1, teachers.size)
        assertEquals("Иванов И.И.", teachers[0].name)
    }

    @Test
    fun testParseGetSchedule() {
        val jsonString = """
        {
            "kind": "group",
            "instance": "ПИ-411",
            "weeks": [
                {
                    "id": 1,
                    "name": "Неделя 1",
                    "current": true,
                    "parity": 1,
                    "days": [
                        {
                            "id": 1,
                            "date": "2026-09-01",
                            "name": "Вторник",
                            "pairs": [
                                {
                                    "id": 1,
                                    "startTime": "08:30:00",
                                    "endTime": "10:00:00",
                                    "lessons": [
                                        {
                                            "id": 101,
                                            "teacher": {"id": 1, "name": "Иванов И.И."},
                                            "subgroup": {"id": 1, "name": "Вся группа"},
                                            "subject": "Базы данных",
                                            "group": "ПИ-411",
                                            "kind": {"id": 1, "name": "Лекция", "shortName": "лек."},
                                            "audience": "101"
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
            ]
        }
        """.trimIndent()

        val parsed = json.decodeFromString<RINHApi.Schedule.Responses.GetSchedule>(jsonString)
        assertEquals("ПИ-411", parsed.instance)
        assertEquals(1, parsed.weeks.size)
        val day = parsed.weeks[0].days[0]
        assertEquals("2026-09-01", day.date)
        assertEquals(1, day.pairs.size)
        val lesson = day.pairs[0].lessons[0]
        assertEquals("Базы данных", lesson.subject)
        assertEquals("Иванов И.И.", lesson.teacher.name)
    }
}
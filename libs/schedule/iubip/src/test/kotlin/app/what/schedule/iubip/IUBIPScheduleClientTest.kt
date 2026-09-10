package app.what.schedule.iubip

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IUBIPScheduleClientTest {

    @Test
    fun testParseIubipGroupsJson() {
        val sampleGroupsJson = """
        {
            "Колледж права": {
                "Ю101": 1,
                "Ю201": 2
            },
            "Информационные технологии": {
                "ИТ101": 1,
                "ИТ201": 2,
                "ИТ301": 3
            }
        }
        """.trimIndent()

        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        val root = json.parseToJsonElement(sampleGroupsJson)
        assertTrue(root is JsonObject)

        val groups = mutableListOf<String>()
        (root as JsonObject).values.forEach { facultyElem ->
            if (facultyElem is JsonObject) {
                facultyElem.entries.forEach { (groupName, _) ->
                    groups.add(groupName.trim())
                }
            }
        }

        assertEquals(5, groups.size)
        assertTrue(groups.contains("Ю101"))
        assertTrue(groups.contains("ИТ101"))
    }

    @Test
    fun testLessonsScheduleCommonTimes() {
        val times = IUBIPLessonsSchedule.COMMON
        assertEquals(8, times.size)
        assertEquals(1, times.first().number)
        assertEquals("08:20", times.first().start.toString())
        assertEquals("09:50", times.first().end.toString())
        assertEquals("20:20", times.last().start.toString())
    }
}
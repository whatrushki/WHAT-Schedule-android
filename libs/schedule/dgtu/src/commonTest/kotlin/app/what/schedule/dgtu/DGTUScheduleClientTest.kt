package app.what.schedule.dgtu

import app.what.schedule.dgtu.models.ApiResponse
import app.what.schedule.dgtu.models.DGTUApi
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DGTUScheduleClientTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun testParseDgtuScheduleWithNullTeacher() {
        val sampleJson = """
        {
            "state": 1,
            "msg": "ok",
            "data": {
                "isCyclicalSchedule": false,
                "rasp": [
                    {
                        "дата": "2026-09-01T00:00:00",
                        "датаНачала": "2026-09-01T08:30:00",
                        "датаОкончания": "2026-09-01T10:00:00",
                        "номерЗанятия": 1,
                        "дисциплина": "Высшая математика",
                        "преподаватель": null,
                        "аудитория": "1-203",
                        "группа": "ИМ35"
                    },
                    {
                        "дата": "2026-09-01T00:00:00",
                        "датаНачала": "2026-09-01T10:15:00",
                        "датаОкончания": "2026-09-01T11:45:00",
                        "номерЗанятия": 2,
                        "дисциплина": "Физика",
                        "преподаватель": "Петров П.П.",
                        "аудитория": "8-401",
                        "группа": "ИМ35"
                    }
                ]
            }
        }
        """.trimIndent()

        val parsed = json.decodeFromString<ApiResponse<DGTUApi.Schedule.Get>>(sampleJson)
        val data = parsed.data
        assertEquals(2, data.rasp.size)

        val firstLesson = data.rasp[0]
        assertEquals("ИМ35", firstLesson.group)
        assertNull(firstLesson.teacher)
        assertEquals("Высшая математика", firstLesson.subject)

        val secondLesson = data.rasp[1]
        assertEquals("Петров П.П.", secondLesson.teacher)
    }
}
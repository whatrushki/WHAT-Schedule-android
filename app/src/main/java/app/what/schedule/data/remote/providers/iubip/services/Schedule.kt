package app.what.schedule.data.remote.providers.iubip.services

import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.schedule.data.remote.api.AdditionalData
import app.what.schedule.data.remote.api.ScheduleResponse
import app.what.schedule.data.remote.api.ScheduleService
import app.what.schedule.data.remote.api.models.DaySchedule
import app.what.schedule.data.remote.api.models.Group
import app.what.schedule.data.remote.api.models.Lesson
import app.what.schedule.data.remote.api.models.LessonState
import app.what.schedule.data.remote.api.models.LessonType
import app.what.schedule.data.remote.api.models.LessonsScheduleType
import app.what.schedule.data.remote.api.models.OneTimeUnit
import app.what.schedule.data.remote.api.models.Teacher
import app.what.schedule.data.remote.providers.iubip.IUBIPLessonsSchedule
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.parameters
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.LocalDateTime

class IUBIPScheduleService(
    private val baseUrl: String,
    private val client: HttpClient
) : ScheduleService {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override suspend fun getGroupSchedule(
        group: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): ScheduleResponse {
        val scheduleTag = buildTag(LogScope.SCHEDULE, LogCat.NET, "iubip")
        Auditor.debug(scheduleTag, "Запрос расписания группы: $group")
        crashlytics.setCustomKey("schedule_group", group)
        crashlytics.setCustomKey("institution", "iubip")

        val response = client
            .submitForm(
                url = "${baseUrl}/local/templates/univer/include/schedule/ajax/read-file-groups.php",
                formParameters = parameters {
                    append("do", "schedule")
                    append("group", group)
                }
            )

        val schedules = Json.parseToJsonElement(response.bodyAsText())
            .jsonObject[group]!!
            .jsonArray[1]
            .jsonObject.values.toList()
            .let { if (it.size > 1) it.slice(0..1) else it }
            .flatMap {
                parseWeek(it.jsonArray.toList()[1])
            }
            .takeIf(List<DaySchedule>::isNotEmpty)
            ?.let { ScheduleResponse.Available.FromSource(it, LocalDateTime.now()) }
            ?: ScheduleResponse.Empty

        Auditor.debug(
            scheduleTag,
            "Получено дней в расписании: ${if (schedules is ScheduleResponse.Available) schedules.schedules.size else 0}"
        )
        return schedules
    }

    private fun parseWeek(week: JsonElement): List<DaySchedule> {
        val days = mutableListOf<DaySchedule>()

        week.jsonObject.entries.forEach { (_, dayScheduleRaw) ->
            var date: LocalDate? = null

            val lessons =
                dayScheduleRaw.jsonObject.entries.map { (lessonNumRaw, otUnitsRaw) ->
                    val number = lessonNumRaw.trim().toInt()
                    val time = IUBIPLessonsSchedule.COMMON.first { it.number == number }
                    val otUnits = otUnitsRaw.jsonArray.map {
                        val auditory = it.jsonObject["AUD"]!!.jsonPrimitive.toString()
                            .replace("\"", "").trim()

                        OneTimeUnit(
                            group = Group(
                                it.jsonObject["GROUP"]!!.jsonPrimitive.toString()
                                    .replace("\"", "").trim()
                            ),
                            teacher = Teacher(
                                it.jsonObject["NAME"]!!.jsonPrimitive.toString()
                                    .replace("\"", "").trim()
                            ),
                            auditory = auditory,
                            building = if ("Дис" in auditory) "*" else "1"
                        )
                    }

                    fun getFromFirstOtUnit(key: String) =
                        otUnitsRaw.jsonArray[0].jsonObject[key]!!.jsonPrimitive.toString()
                            .replace("\"", "").trim()

                    if (date == null) date = getFromFirstOtUnit("DATE").let {
                        val raw = it.split("-").map(String::toInt)
                        LocalDate.of(raw[2], raw[1], raw[0]).also {
                            if (it < LocalDate.now()) return@forEach
                        }
                    }

                    Lesson(
                        date = date,
                        number = number,
                        startTime = time.startTime,
                        endTime = time.endTime,
                        subject = getFromFirstOtUnit("SUBJECT"),
                        type = when (getFromFirstOtUnit("SUBJ_TYPE")) {
                            "Урок" -> LessonType.COMMON
                            else -> LessonType.LECTURE
                        },
                        state = if (getFromFirstOtUnit("deleted").toInt() == 1) LessonState.REMOVED
                        else LessonState.COMMON,
                        otUnits = otUnits
                    )
                }

            days.add(
                DaySchedule(
                    date = date!!,
                    scheduleType = LessonsScheduleType.COMMON,
                    lessons = lessons
                )
            )
        }

        return days
    }

    override suspend fun getTeacherSchedule(
        teacher: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): ScheduleResponse = ScheduleResponse.Empty

    override suspend fun getGroups(): List<Group> {
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "iubip")
        Auditor.debug(netTag, "Загрузка списка групп")

        val groups = client
            .submitForm(
                url = "${baseUrl}/local/templates/univer/include/schedule/ajax/read-file-groups.php",
                formParameters = parameters {
                    append("do", "groups")
                }
            )
            .body<Map<String, Map<String, Int>>>().values
            .flatMap {
                it.keys.map { Group(it) }
            }

        Auditor.debug(netTag, "Загружено групп: ${groups.size}")
        return groups
    }

    override suspend fun getTeachers(): List<Teacher> = emptyList()
}
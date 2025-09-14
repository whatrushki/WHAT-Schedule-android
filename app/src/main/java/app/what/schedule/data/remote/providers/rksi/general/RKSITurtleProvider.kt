package app.what.schedule.data.remote.providers.rksi.general

import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.schedule.data.remote.api.AdditionalData
import app.what.schedule.data.remote.api.DaySchedule
import app.what.schedule.data.remote.api.Group
import app.what.schedule.data.remote.api.InstitutionProvider
import app.what.schedule.data.remote.api.Lesson
import app.what.schedule.data.remote.api.LessonTime
import app.what.schedule.data.remote.api.LessonType
import app.what.schedule.data.remote.api.LessonsScheduleType
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.OneTimeUnit
import app.what.schedule.data.remote.api.ParseMode
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.api.Teacher
import app.what.schedule.data.remote.providers.rksi.general.RKSILessonsSchedule.numberOf
import app.what.schedule.data.remote.providers.rksi.general.TurtleApi.Schedule.Responses.GetSchedule
import app.what.schedule.data.remote.utils.parseMonth
import app.what.schedule.data.remote.utils.parseTime
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.LocalDate
import java.time.LocalTime

private val TurtleProviderMetadata by lazy {
    MetaInfo(
        id = "turtle",
        name = "Turtle",
        fullName = "Turtle Schedule",
        description = "Неофициальный api-провайдер команды Turtle",
        sourceTypes = setOf(SourceType.API),
        sourceUrl = "https://rksi.ru",
        advantages = listOf(
            "Быстрая загрузка (~2с)"
        ),
        disadvantages = listOf(
            "Нет сокращенного расписания звонков",
            "Незначительные ошибки в расписании"
        )
    )
}

class RKSITurtleProvider(
    private val client: HttpClient
) : InstitutionProvider {
    companion object Factory : InstitutionProvider.Factory, KoinComponent {
        private const val BASE_URL = "http://45.155.207.232:8080/api/v2"
        override val metadata = TurtleProviderMetadata
        override fun create(): InstitutionProvider = RKSITurtleProvider(get())
    }

    override val metadata = Factory.metadata
    override val lessonsSchedule = RKSILessonsSchedule

    override suspend fun getGroupSchedule(
        group: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): List<DaySchedule> = client
        .get("$BASE_URL/schedule/$group")
        .body<GetSchedule>()
        .toDaySchedules(ParseMode.GROUP)

    override suspend fun getTeacherSchedule(
        teacher: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): List<DaySchedule> = client
        .get("$BASE_URL/schedule/$teacher")
        .body<GetSchedule>()
        .toDaySchedules(ParseMode.TEACHER)

    override suspend fun getGroups(): List<Group> = client
        .get("$BASE_URL/schedule/list")
        .body<Map<String, List<String>>>()
        .get("group")!!
        .map { Group(it) }

    override suspend fun getTeachers(): List<Teacher> = client
        .get("$BASE_URL/schedule/list")
        .body<Map<String, List<String>>>()
        .get("teacher")!!
        .map { Teacher(it) }

    private fun GetSchedule.toDaySchedules(
        parseMode: ParseMode
    ): List<DaySchedule> = this.days.mapIndexed { index, it ->
        val dataRaw = it.day.split(" ")
        val date = LocalDate.of(
            LocalDate.now().year,
            parseMonth(dataRaw[1].substring(0, dataRaw[1].length - 1)),
            dataRaw.first().toInt()
        )


        var lessons: List<Lesson> = it.apairs.map {
            val lesson = it.apair.first()

            Auditor.debug(
                "d",
                "Lesson: ${lesson.doctrine} ${lesson.start} ${lesson.end} ${lesson.corpus}"
            )

            val otUnits = it.apair.map { otUnit ->
                OneTimeUnit(
                    teacher = Teacher(if (parseMode == ParseMode.TEACHER) name else otUnit.teacher),
                    group = Group(if (parseMode == ParseMode.TEACHER) otUnit.teacher else name),
                    building = otUnit.corpus,
                    auditory = otUnit.auditory
                )
            }

            Lesson(
                number = lesson.number,
                startTime = parseTime(lesson.start),
                endTime = parseTime(lesson.end),
                subject = lesson.doctrine,
                type = when {
                    "Доп." in lesson.doctrine -> LessonType.ADDITIONAL
                    "Классный" in lesson.doctrine -> LessonType.CLASS_HOUR
                    else -> LessonType.COMMON
                },
                otUnits = otUnits
            )
        }.toMutableList().apply {
            val groupedLessons = groupBy { it.startTime }
            clear()

            groupedLessons.forEach { (_, l) ->
                var unionLesson = l.first()
                l.forEachIndexed { index, it -> if (index != 0) unionLesson += it }
                add(unionLesson)
            }
        }

        var schedule = RKSILessonsSchedule.COMMON

        lessons = if (lessons.firstOrNull { it.type == LessonType.CLASS_HOUR } != null) {
            schedule = RKSILessonsSchedule.WITH_CLASS_HOUR
            lessons.map {
                it.copy(
                    number = RKSILessonsSchedule.WITH_CLASS_HOUR.numberOf(it.startTime) ?: 0
                )
            }
        } else {
            val getNumberOrChangeSchedule = { time: LocalTime, change: List<LessonTime> ->
                schedule.numberOf(time) ?: let { schedule = change; null }
            }

            lessons.map { lesson ->
                val number =
                    getNumberOrChangeSchedule(lesson.startTime, RKSILessonsSchedule.WITH_CLASS_HOUR)
                        ?: getNumberOrChangeSchedule(
                            lesson.startTime,
                            RKSILessonsSchedule.SHORTENED
                        )
                        ?: getNumberOrChangeSchedule(lesson.startTime, RKSILessonsSchedule.COMMON)
                        ?: 0

                lesson.copy(number = number)
            }
        }.sortedBy { it.startTime }

        DaySchedule(
            date = date,
            dateDescription = it.day,
            lessons = lessons,
            scheduleType = when (schedule) {
                RKSILessonsSchedule.COMMON -> LessonsScheduleType.COMMON
                RKSILessonsSchedule.SHORTENED -> LessonsScheduleType.SHORTENED
                RKSILessonsSchedule.WITH_CLASS_HOUR -> LessonsScheduleType.WITH_CLASS_HOUR
                else -> LessonsScheduleType.COMMON
            }
        )
    }
}


private object TurtleApi {
    object Schedule {
        object Responses {
            @Serializable
            data class GetSchedule(
                val days: List<ForDay>,
                val name: String
            )

            @Serializable
            data class ForDay(
                val day: String,
                val isoDateDay: String,
                val apairs: List<Apair>
            )

            @Serializable
            data class Apair(
                val time: String,
                val apair: List<ApairApair>,
                val isoDateStart: String,
                val isoDateEnd: String
            )

            @Serializable
            data class ApairApair(
                val doctrine: String,
                val teacher: String,
                @SerialName("auditoria") val auditory: String,
                val corpus: String,
                val number: Int,
                val start: String,
                val end: String,
                val warn: String
            )
        }
    }
}
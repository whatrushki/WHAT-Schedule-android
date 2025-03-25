package app.what.schedule.data.remote.providers.dgtu.general

import app.what.foundation.utils.asyncLazy
import app.what.schedule.data.remote.api.AdditionalData
import app.what.schedule.data.remote.api.DaySchedule
import app.what.schedule.data.remote.api.Group
import app.what.schedule.data.remote.api.InstitutionProvider
import app.what.schedule.data.remote.api.Lesson
import app.what.schedule.data.remote.api.LessonState
import app.what.schedule.data.remote.api.LessonType
import app.what.schedule.data.remote.api.LessonsScheduleType
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.OneTimeUnit
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.api.Teacher
import app.what.schedule.data.remote.providers.rksi.general.RKSILessonsSchedule
import app.what.schedule.data.remote.utils.LocalDateTimeSerializer
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val DGTUProviderMetadata by lazy {
    MetaInfo(
        id = "dgtu",
        name = "ДГТУ",
        fullName = "Расписание Дгту",
        description = "Официальный api-провайдер",
        sourceTypes = setOf(SourceType.API),
        sourceUrl = "https://edu.donstu.ru/WebApp/#/Rasp",
        advantages = listOf(),
        disadvantages = listOf()
    )
}

class DGTUOfficialProvider(
    private val client: HttpClient
) : InstitutionProvider {
    companion object Factory : InstitutionProvider.Factory, KoinComponent {
        private const val BASE_URL = "https://edu.donstu.ru/api"
        override val metadata = DGTUProviderMetadata
        override fun create(): InstitutionProvider = DGTUOfficialProvider(get())
    }

    override val metadata = Factory.metadata
    override val lessonsSchedule = RKSILessonsSchedule

    private val listYears by asyncLazy { listYears() }

    private suspend fun listYears() = client
        .get("$BASE_URL/Rasp/ListYears")
        .body<ApiResponse<DGTUApi.Schedule.Responses.ListYears>>()
        .data.years

    override suspend fun getGroupSchedule(
        group: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): List<DaySchedule> = client
        .get(
            "$BASE_URL/Rasp?idGroup=$group&sdate=${
                LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            }"
        )
        .body<ApiResponse<DGTUApi.Schedule.Responses.GetSchedule>>()
        .data.rasp.toDaySchedules()

    override suspend fun getTeacherSchedule(
        teacher: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): List<DaySchedule> = client
        .get(
            "$BASE_URL/Rasp?idTeacher=$teacher&sdate=${
                LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            }"
        )
        .body<ApiResponse<DGTUApi.Schedule.Responses.GetSchedule>>()
        .data.rasp.toDaySchedules()

    override suspend fun getGroups(): List<Group> = client
        .get("$BASE_URL/raspGrouplist?year=${listYears.await().last()}")
        .body<ApiResponse<List<DGTUApi.Schedule.Responses.DGTUGroup>>>()
        .data.map { Group(it.name, it.id.toString(), it.kurs) }
        .sortedBy { it.name }

    override suspend fun getTeachers(): List<Teacher> = client
        .get("$BASE_URL/raspTeacherlist?year=${listYears.await().last()}")
        .body<ApiResponse<List<DGTUApi.Schedule.Responses.DGTUTeacher>>>()
        .data.map {
            Teacher(it.name.split(" ").let {
                it[0] + it.mapIndexedNotNull { index, s ->
                    if (index in 1..2 && s.isNotEmpty()) "${s[0]}." else null
                }.joinToString("")
            }, it.id.toString())
        }
        .sortedBy { it.name }


    private fun List<DGTUApi.Schedule.Responses.DGTULesson>.toDaySchedules() =
        groupBy { it.date.toLocalDate() }.map { (day, lessons) ->
            DaySchedule(day, "", LessonsScheduleType.COMMON, lessons.map { it.toLesson() })
        }

    private fun DGTUApi.Schedule.Responses.DGTULesson.toLesson(): Lesson {
        val rawData = this.auditory.split("-")
        val building = rawData[0]
        val auditory = rawData[1]

        return Lesson(
            number = this.number,
            startTime = this.startTime.toLocalTime(),
            endTime = endTime.toLocalTime(),
            subject = subject,
            type = LessonType.COMMON,
            state = if (replacement) LessonState.CHANGED
            else LessonState.COMMON,
            otUnits = listOf(
                OneTimeUnit(
                    group = Group(group, codeGroup.toString()),
                    teacher = Teacher(teacher, codeTeacher.toString()),
                    building = building,
                    auditory = auditory
                )
            )
        )
    }
}

@Serializable
private data class ApiResponse<T>(
    val data: T,
    val state: Int,
    val msg: String,
    val time: Float
)

private object DGTUApi {
    object Schedule {
        object Responses {
            @Serializable
            data class ListYears(
                val years: List<String>
            )

            @Serializable
            data class GetSchedule(
                val isCyclicalSchedule: Boolean,
                val rasp: List<DGTULesson>,
//                val info: LessonInfo #не нужно
            )

            @Serializable
            data class DGTUTeacher(
                val name: String,
                val id: Int
            )

            @Serializable
            data class DGTUGroup(
                val name: String,
                val id: Int,
                val kurs: Int
            )

            @Serializable
            data class DGTULesson(
                @SerialName("код") val code: Int,
                @SerialName("дата") @Serializable(LocalDateTimeSerializer::class)
                val date: LocalDateTime,
                @SerialName("датаНачала") @Serializable(LocalDateTimeSerializer::class)
                val startTime: LocalDateTime,
                @SerialName("датаОкончания") @Serializable(LocalDateTimeSerializer::class)
                val endTime: LocalDateTime,
                @SerialName("перерыв") val breakTime: Float?,
                @SerialName("начало") val start: String,
                @SerialName("конец") val end: String,
                @SerialName("деньНедели") val weekDays: Int,
                @SerialName("день_недели") val weekDay: String,
                @SerialName("почта") val email: String,
                @SerialName("день") val day: String,
                @SerialName("код_Семестра") val codeSemester: Int,
                @SerialName("типНедели") val weekType: Int,
                @SerialName("номерПодгруппы") val numberSubgroup: Int,
                @SerialName("часов") val hoursOf: String?,
                @SerialName("дисциплина") val subject: String,
                @SerialName("преподаватель") val teacher: String,
                @SerialName("должность") val position: String?,
                @SerialName("аудитория") val auditory: String,
                @SerialName("учебныйГод") val studyYear: String,
                @SerialName("группа") val group: String,
                @SerialName("custom1") val custom1: String,
                @SerialName("часы") val hours: String,
                @SerialName("неделяНачала") val weekOfStart: Int,
                @SerialName("неделяОкончания") val weekOfEnd: Int,
                @SerialName("замена") val replacement: Boolean,
                @SerialName("кодПреподавателя") val codeTeacher: Int,
                @SerialName("кодГруппы") val codeGroup: Int,
                @SerialName("фиоПреподавателя") val teacherName: String,
                @SerialName("кодПользователя") val codeUser: Int,
                @SerialName("элементЦиклРасписания") val cycleElement: Boolean,
                @SerialName("элементГрафика") val graphElement: Boolean,
                @SerialName("тема") val theme: String,
                @SerialName("номерЗанятия") val number: Int,
                @SerialName("ссылка") val link: String?,
                @SerialName("созданиеВебинара") val createWebinar: Boolean,
                @SerialName("кодВебинара") val codeWebinar: Int?,
                @SerialName("вебинарЗапущен") val webinarStarted: Boolean,
                @SerialName("показатьЖурнал") val showJournal: Boolean,
                @SerialName("кодыСтрок") val codeLines: List<Int>,
                @SerialName("цвет") val color: String
            )
        }
    }
}

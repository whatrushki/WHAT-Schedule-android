package app.what.schedule.data.remote.providers.rinh.general


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
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalTime

private val RINHProviderMetadata
    get() = MetaInfo(
        id = "rinh",
        name = "Digital",
        fullName = "Расписание РГЭУ (РИНХ)",
        description = "Неофициальный api-провайдер команды Digital",
        sourceTypes = setOf(SourceType.API),
        sourceUrl = "https://rasp.rsue.ru",
        advantages = listOf(),
        disadvantages = listOf()
    )

class RINHOfficialProvider(
    private val client: HttpClient
) : InstitutionProvider {
    companion object Factory : InstitutionProvider.Factory, KoinComponent {
        private const val BASE_URL = "https://rasp-api.rsue.ru/api"
        override val metadata by lazy { RINHProviderMetadata }
        override fun create(): InstitutionProvider = RINHOfficialProvider(get())
    }

    override val metadata = Factory.metadata
    override val lessonsSchedule = RKSILessonsSchedule

    private val getGroupsAndTeachers by asyncLazy { getGroupsAndTeachers() }

    private suspend fun getGroupsAndTeachers() = client
        .get("$BASE_URL/v1/schedule/search?format=json")
        .body<List<RINHApi.Schedule.Responses.ScheduleSearch>>()

    private suspend fun getSchedule(value: String) = client
        .get(withContext(IO) {
            "$BASE_URL/v1/schedule/lessons/${
                URLEncoder.encode(
                    value,
                    "UTF-8"
                ).replace("+", "%20")
            }?format=json"
        })
        .body<RINHApi.Schedule.Responses.GetSchedule>()
        .toDaySchedules()


    override suspend fun getGroupSchedule(
        group: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): List<DaySchedule> = getSchedule(group)

    override suspend fun getTeacherSchedule(
        teacher: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): List<DaySchedule> = getSchedule(teacher)

    override suspend fun getGroups(): List<Group> = getGroupsAndTeachers
        .await()
        .filter { "," !in it.name && "." !in it.name && "№" !in it.name }
        .map { Group(it.name.trim()) }

    override suspend fun getTeachers(): List<Teacher> = getGroupsAndTeachers
        .await()
        .filter { "," in it.name || "." in it.name || "№" in it.name }
        .map { Teacher(it.name) }


    private fun RINHApi.Schedule.Responses.GetSchedule.toDaySchedules(): List<DaySchedule> {
        val now = LocalDate.now()

        return weeks.map {
            it.days.filter { it.date.toLocalDate() >= now && it.pairs.any { it.lessons.isNotEmpty() } }
        }.flatten().map {
            DaySchedule(
                date = it.date.toLocalDate(),
                dateDescription = "${it.date} ${it.name}",
                scheduleType = LessonsScheduleType.COMMON,
                lessons = it.pairs.filterNot { it.lessons.isEmpty() }.map { it.toLesson() }
            )
        }
    }

    private fun RINHApi.Schedule.Responses.APair.toLesson(): Lesson = Lesson(
        number = id,
        startTime = startTime.toLocalTime(),
        endTime = endTime.toLocalTime(),
        subject = "${lessons.first().kind.shortName} ${lessons.first().subject}",
        type = when (lessons.first().kind.id) {
            1 -> LessonType.LECTURE
            2 -> LessonType.PRACTISE
            3 -> LessonType.LABORATORY
            5 -> LessonType.CREDIT
            else -> LessonType.COMMON
        },
        state = LessonState.COMMON,
        otUnits = lessons.map {
            OneTimeUnit(
                group = Group(it.group),
                teacher = Teacher(it.teacher.name),
                auditory = it.audience.let { if (it[0].isDigit() || it[0] == 'с') it else it.drop(1) },
                building = when (it.audience[0]) {
                    '*' -> "2"
                    '#' -> "3"
                    '&' -> "4"
                    'д' -> "д"
                    else -> "1"
                }
            )
        }
    )
}

private fun String.toLocalTime() =
    split(":").let { LocalTime.of(it[0].toInt(), it[1].toInt(), it[2].toInt()) }

private fun String.toLocalDate() =
    split(".").let { LocalDate.of(it[2].toInt(), it[1].toInt(), it[0].toInt()) }

private object RINHApi {
    object Schedule {
        object Responses {
            @Serializable
            data class ScheduleSearch(
                val id: Int,
                val name: String
            )

            @Serializable
            data class GetSchedule(
                val kind: String,
                val instance: String,
                val weeks: List<Week>
            )

            @Serializable
            data class Week(
                val id: Int,
                val name: String,
                val current: Boolean,
                val parity: Int,
                val days: List<Day>,
            )

            @Serializable
            data class Day(
                val id: Int,
                val date: String,
                val name: String,
                val pairs: List<APair>
            )

            @Serializable
            data class APair(
                val id: Int,
                val startTime: String,
                val endTime: String,
                val lessons: List<RINHLesson>
            )

            @Serializable
            data class RINHLesson(
                val id: Int,
                val teacher: RINHTeacher,
                val subgroup: SubGroup,
                val subject: String,
                val group: String,
                val kind: Kind,
                val audience: String
            )

            @Serializable
            data class Kind(
                val id: Int,
                val name: String,
                val shortName: String
            )

            @Serializable
            data class SubGroup(
                val id: Int,
                val name: String
            )

            @Serializable
            data class RINHTeacher(
                val id: Int,
                val name: String
            )
        }
    }
}

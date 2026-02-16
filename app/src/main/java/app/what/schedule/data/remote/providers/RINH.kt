package app.what.schedule.data.remote.providers

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.asyncLazy
import app.what.schedule.data.remote.api.AdditionalData
import app.what.schedule.data.remote.api.Institution
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.ScheduleResponse
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.api.models.DaySchedule
import app.what.schedule.data.remote.api.models.Group
import app.what.schedule.data.remote.api.models.Lesson
import app.what.schedule.data.remote.api.models.LessonState
import app.what.schedule.data.remote.api.models.LessonType
import app.what.schedule.data.remote.api.models.LessonsScheduleType
import app.what.schedule.data.remote.api.models.NewContent
import app.what.schedule.data.remote.api.models.NewItem
import app.what.schedule.data.remote.api.models.NewListItem
import app.what.schedule.data.remote.api.models.NewTag
import app.what.schedule.data.remote.api.models.OneTimeUnit
import app.what.schedule.data.remote.api.models.Teacher
import app.what.schedule.data.remote.utils.parseMonth
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private val RINHMetadata
    get() = MetaInfo(
        id = "rinh",
        name = "РИНХ",
        fullName = "Ростовский Государственный Экономический Университет",
        description = "Ростовский Государственный Экономический Университет",
        sourceTypes = setOf(SourceType.API),
        sourceUrl = "https://rasp.rsue.ru",
    )

class RINH(
    private val client: HttpClient,
    private val scope: CoroutineScope
) : Institution {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    companion object Factory : Institution.Factory, KoinComponent {
        private const val SCHEDULE_BASE_URL = "https://www.iubip.ru"
        private const val NEWS_BASE_URL = "https://rsue.ru"
        override val metadata by lazy { RINHMetadata }
        override fun create(): Institution = RINH(get(), get())
    }

    override val metadata = Factory.metadata

    private val getGroupsAndTeachers by scope.asyncLazy { getGroupsAndTeachers() }

    private suspend fun getGroupsAndTeachers() = client
        .get("$SCHEDULE_BASE_URL/v1/schedule/search?format=json")
        .body<List<RINHApi.Schedule.Responses.ScheduleSearch>>()

    private suspend fun getSchedule(value: String): ScheduleResponse {
        val scheduleTag = buildTag(LogScope.SCHEDULE, LogCat.NET, "rinh")
        Auditor.debug(scheduleTag, "Запрос расписания: $value")
        crashlytics.setCustomKey("schedule_value", value)
        crashlytics.setCustomKey("institution", "rinh")

        val encodedValue = withContext(IO) {
            URLEncoder.encode(value, "UTF-8").replace("+", "%20")
        }

        val schedules = client
            .get("$SCHEDULE_BASE_URL/v1/schedule/lessons/$encodedValue?format=json")
            .body<RINHApi.Schedule.Responses.GetSchedule>()
            .toDaySchedules()
            .takeIf(List<DaySchedule>::isNotEmpty)
            ?.let { ScheduleResponse.Available.FromSource(it, LocalDateTime.now()) }
            ?: ScheduleResponse.Empty

        Auditor.debug(
            scheduleTag,
            "Получено дней в расписании: ${if (schedules is ScheduleResponse.Available) schedules.schedules.size else 0}"
        )
        return schedules
    }


    override suspend fun getGroupSchedule(
        group: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): ScheduleResponse = getSchedule(group)

    override suspend fun getTeacherSchedule(
        teacher: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): ScheduleResponse = getSchedule(teacher)

    override suspend fun getGroups(): List<Group> {
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "rinh")
        Auditor.debug(netTag, "Загрузка списка групп")

        val groups = getGroupsAndTeachers
            .await()
            .filter { "," !in it.name && "." !in it.name && "№" !in it.name }
            .map { Group(it.name.trim()) }

        Auditor.debug(netTag, "Загружено групп: ${groups.size}")
        return groups
    }

    override suspend fun getTeachers(): List<Teacher> {
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "rinh")
        Auditor.debug(netTag, "Загрузка списка преподавателей")

        val teachers = getGroupsAndTeachers
            .await()
            .filter { "," in it.name || "." in it.name || "№" in it.name }
            .map { Teacher(it.name) }

        Auditor.debug(netTag, "Загружено преподавателей: ${teachers.size}")
        return teachers
    }

    override suspend fun getNews(page: Int): List<NewListItem> {
        val response = client.get("$NEWS_BASE_URL/universitet/novosti/?PAGEN_2=$page").bodyAsText()
        val document = Ksoup.parse(response)
        val rawData = document.getElementsByClass("news-item")
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "rinh")
        Auditor.debug(netTag, "Получено новостей: ${rawData.size}")

        val data = rawData.map {
            val url = it.getElementsByTag("a").attr("href")
            val id = url.split("=").last()
            val bannerUrl = formatImageUrl(it.getElementsByTag("img").attr("src"))
            val title = it.getElementsByTag("a").first()!!.text()
            val description = null
            val date = it.getElementById("news-date")!!.text().let {
                val tmp = it.split(" ")
                LocalDate.of(tmp[2].toInt(), parseMonth(tmp[1]), tmp[0].toInt())
            }
            val tags = emptyList<NewTag>()

            NewListItem(id, url, bannerUrl, title, description, date, tags)
        }

        Auditor.debug(netTag, "Обработано новостей: ${data.size}")

        return data
    }

    override suspend fun getNewDetail(id: String): NewItem {
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "rinh")
        Auditor.debug(netTag, "Загрузка деталей новости: $id")

        val url = "$NEWS_BASE_URL/universitet/novosti/novosti.php?ELEMENT_ID=$id"
        val response = client.get(url).bodyAsText()
        val document = Ksoup.parse(response)

        val bannerUrl = formatImageUrl(document.getElementsByTag("img").attr("src"))
        val title = document.getElementsByTag("h1").first()!!.text()
        val description = null
        val date = document.getElementById("date-news")!!.text().let {
            val tmp = it.split(" ")
            LocalDate.of(tmp[2].toInt(), parseMonth(tmp[1]), tmp[0].toInt())
        }
        val tags = emptyList<NewTag>()
        val content = parseNewContent(document.getElementById("text-news")!!)
            .then(document.getElementsByClass("slider-news").first()?.let { parseNewContent(it) })

        Auditor.debug(netTag, "Новость успешно загружена: $title")
        return NewItem(id, url, bannerUrl, title, description, tags, date, content)
    }

    private fun parseNewContent(tree: Element): NewContent {
        val list = mutableListOf<NewContent>()

        tree.children().forEach {
            val contentItem = when {
                it.`is`("p") && it.text()
                    .isNotBlank() -> NewContent.Item.Text(AnnotatedString.fromHtml(it.html()))

                it.`is`(".owl-carousel") -> NewContent.Item.ImageCarousel(
                    it.getElementsByTag("img").map { formatImageUrl(it.attr("src")) }
                )

                it.`is`("ul") -> NewContent.Item.UnsortedList(
                    it.getElementsByTag("li").map { it.text() })

                it.`is`("ol") -> NewContent.Item.SortedList(
                    it.getElementsByTag("li").map { it.text() })

                else -> null
            }

            contentItem ?: return@forEach
            list.add(contentItem)
        }

        return NewContent.Container.Column(list)
    }

    private fun formatImageUrl(url: String): String = NEWS_BASE_URL + url

    private fun RINHApi.Schedule.Responses.GetSchedule.toDaySchedules(): List<DaySchedule> {
        val now = LocalDate.now()

        return weeks.map {
            it.days.filter { it.date.toLocalDate() >= now && it.pairs.any { it.lessons.isNotEmpty() } }
        }.flatten().map {
            DaySchedule(
                date = it.date.toLocalDate(),
                scheduleType = LessonsScheduleType.COMMON,
                lessons = it.pairs.filterNot { it.lessons.isEmpty() }
                    .map { raw -> raw.toLesson(it.date.toLocalDate()) }
            )
        }
    }

    private fun RINHApi.Schedule.Responses.APair.toLesson(date: LocalDate): Lesson = Lesson(
        date = date,
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
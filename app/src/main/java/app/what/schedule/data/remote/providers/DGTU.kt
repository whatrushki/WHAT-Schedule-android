package app.what.schedule.data.remote.providers

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.util.fastJoinToString
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.asyncLazy
import app.what.schedule.data.remote.api.AdditionalData
import app.what.schedule.data.remote.api.Institution
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.ScheduleResponse
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.api.models.AuthorInfo
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
import app.what.schedule.data.remote.utils.LocalDateTimeSerializer
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val DGTUProviderMetadata
    get() = MetaInfo(
        id = "dgtu",
        name = "ДГТУ",
        fullName = "Донской Государственный Технический Университет",
        description = "Донской Государственный Технический Университет",
        sourceTypes = setOf(SourceType.API),
        sourceUrl = "https://edu.donstu.ru/WebApp/#/Rasp",
    )

class DGTU(
    private val client: HttpClient,
    private val scope: CoroutineScope
) : Institution {
    companion object Factory : Institution.Factory, KoinComponent {
        private const val SCHEDULE_BASE_URL = "https://edu.donstu.ru/api"
        private const val NEWS_BASE_URL = "https://news.donstu.ru"
        override val metadata by lazy { DGTUProviderMetadata }
        override fun create(): Institution = DGTU(get(), get())
    }

    override val metadata = Factory.metadata

    private val listYears by scope.asyncLazy { listYears() }

    private suspend fun listYears() = client
        .get("$SCHEDULE_BASE_URL/Rasp/ListYears")
        .body<ApiResponse<DGTUApi.Schedule.Responses.ListYears>>()
        .data.years

    override suspend fun getGroupSchedule(
        group: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): ScheduleResponse = client
        .get(
            "$SCHEDULE_BASE_URL/Rasp?idGroup=$group&sdate=${
                LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            }"
        )
        .body<ApiResponse<DGTUApi.Schedule.Responses.GetSchedule>>()
        .data.rasp.toDaySchedules()

    override suspend fun getTeacherSchedule(
        teacher: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): ScheduleResponse = client
        .get(
            "$SCHEDULE_BASE_URL/Rasp?idTeacher=$teacher&sdate=${
                LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            }"
        )
        .body<ApiResponse<DGTUApi.Schedule.Responses.GetSchedule>>()
        .data.rasp.toDaySchedules()

    override suspend fun getGroups(): List<Group> = client
        .get("$SCHEDULE_BASE_URL/raspGrouplist?year=${listYears.await().last()}")
        .body<ApiResponse<List<DGTUApi.Schedule.Responses.DGTUGroup>>>()
        .data.map { Group(it.name, it.id.toString(), it.kurs) }
        .sortedBy { it.name }

    override suspend fun getTeachers(): List<Teacher> = client
        .get("$SCHEDULE_BASE_URL/raspTeacherlist?year=${listYears.await().last()}")
        .body<ApiResponse<List<DGTUApi.Schedule.Responses.DGTUTeacher>>>()
        .data.map {
            Teacher(it.name.split(" ").let {
                it[0] + it.mapIndexedNotNull { index, s ->
                    if (index in 1..2 && s.isNotEmpty()) "${s[0]}." else null
                }.joinToString("")
            }, it.id.toString())
        }
        .sortedBy { it.name }

    override suspend fun getNews(page: Int): List<NewListItem> {
        val url = "$NEWS_BASE_URL/news/?PAGEN_2=$page"
        val response = client.get(url).bodyAsText()
        val document = Ksoup.parse(response)
        val rawData = document.getElementsByClass("news-card")

        Auditor.debug("d", "news " + rawData.size)

        val data = rawData.map {
            val url = it.getElementsByTag("a").attr("href")
            val id = url.split("/").last()
            val bannerUrl = formatImageUrl(it.getElementsByTag("img").attr("src"))
            val title = it.getElementsByTag("h4").first()!!.text()
            val description = null
            val date = it.getElementsByTag("time").attr("datetime").let {
                val tmp = it.split(" ").first().split(".").map(String::toInt)
                LocalDate.of(tmp[2], tmp[1], tmp[0])
            }
            val tags = it.getElementsByClass("tag")
                .map { NewTag(it.text(), it.attr("href").split("=").last()) }

            NewListItem(id, url, bannerUrl, title, description, date, tags)
        }

        Auditor.debug("d", "news " + data.fastJoinToString())

        return data
    }

    override suspend fun getNewDetail(id: String): NewItem {
        val url = "$NEWS_BASE_URL/news/?PAGEN_2=$id"
        val response = client.get("$NEWS_BASE_URL/news/$id").bodyAsText()
        val document = Ksoup.parse(response)

        val bannerUrl = formatImageUrl(document.getElementsByTag("img").attr("src"))
        val title = document.getElementsByTag("h1").text().split(" ").dropLast(1).joinToString(" ")
        val description = document.getElementsByClass("detail-hero__subtitle").html()
        val date = document.getElementsByTag("time").attr("datetime").let {
            val tmp = it.split(" ").first().split(".").map(String::toInt)
            LocalDate.of(tmp[2], tmp[1], tmp[0])
        }
        val tags = document.getElementsByClass("detail-hero__card")[0].getElementsByClass("tag")
            .map { NewTag(it.text(), it.attr("href").split("=").last()) }
        val content =
            parseNewContent(document.selectFirst("div.app-section._gutter-md.container._md.text-content")!!)

        return NewItem(
            id,
            url,
            bannerUrl,
            title,
            AnnotatedString
                .fromHtml(description)
                .takeIf { it.isNotBlank() },
            tags,
            date,
            content
        )
    }

    private fun parseNewContent(tree: Element): NewContent {
        val list = mutableListOf<NewContent>()

        tree.children().forEach {
            Auditor.debug("d", it.outerHtml())

            val contentItem = when {
                it.`is`("p") && it.text()
                    .isNotBlank() -> NewContent.Item.Text(AnnotatedString.fromHtml(it.html()))

                it.`is`("section") && it.getElementsByClass("gallery")
                    .isNotEmpty() -> NewContent.Item.ImageCarousel(
                    it.getElementsByClass("gallery__thumbs-item").map {
                        formatImageUrl(it.getElementsByTag("img").attr("src"))
                    }
                )

                it.`is`("blockquote") -> NewContent.Item.Quote(
                    author = AuthorInfo(
                        it.selectFirst(".blockqoute__img")
                            ?.getElementsByTag("img")
                            ?.attr("src")
                            ?.let { formatImageUrl(it) },
                        it.getElementsByClass("blockqoute__author-name").text(),
                        it.getElementsByClass("blockqoute__author-post").text()
                    ),
                    data = it.getElementsByClass("blockqoute__content")[0].getElementsByTag("p")[0].text()
                )

                it.`is`(".highlight") -> NewContent.Item.Info(
                    it.getElementsByClass("highlight__content")[0].getElementsByTag("p")[0].text()
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


    private fun List<DGTUApi.Schedule.Responses.DGTULesson>.toDaySchedules() =
        groupBy { it.date.toLocalDate() }.map { (day, lessons) ->
            DaySchedule(day, LessonsScheduleType.COMMON, lessons.map { it.toLesson(day) })
        }.takeIf(List<DaySchedule>::isNotEmpty)
            ?.let { ScheduleResponse.Available.FromSource(it, LocalDateTime.now()) }
            ?: ScheduleResponse.Empty

    private fun DGTUApi.Schedule.Responses.DGTULesson.toLesson(date: LocalDate): Lesson {
        val rawData = this.auditory.split("-")
        val building = rawData[0]
        val auditory = rawData[1]

        return Lesson(
            date = date,
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
                val kurs: Int?
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
                @SerialName("тема") val theme: String?,
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

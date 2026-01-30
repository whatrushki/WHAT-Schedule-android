package app.what.schedule.data.remote.providers


import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.util.fastJoinToString
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.google.firebase.crashlytics.FirebaseCrashlytics
import app.what.schedule.data.remote.api.AdditionalData
import app.what.schedule.data.remote.api.Institution
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.ScheduleResponse
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.api.models.DaySchedule
import app.what.schedule.data.remote.api.models.Group
import app.what.schedule.data.remote.api.models.Lesson
import app.what.schedule.data.remote.api.models.LessonState
import app.what.schedule.data.remote.api.models.LessonTime
import app.what.schedule.data.remote.api.models.LessonType
import app.what.schedule.data.remote.api.models.LessonsScheduleType
import app.what.schedule.data.remote.api.models.NewContent
import app.what.schedule.data.remote.api.models.NewItem
import app.what.schedule.data.remote.api.models.NewListItem
import app.what.schedule.data.remote.api.models.NewTag
import app.what.schedule.data.remote.api.models.OneTimeUnit
import app.what.schedule.data.remote.api.models.Teacher
import app.what.schedule.data.remote.utils.parseMonth
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.parameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private val IUBIPMetadata
    get() = MetaInfo(
        id = "iubip",
        name = "ИУБиП",
        fullName = "Южный Университет (Институт Управления, Бизнеса и Права)",
        description = "Южный Университет (Институт Управления, Бизнеса и Права)",
        sourceTypes = setOf(SourceType.API, SourceType.PARSER),
        sourceUrl = "https://iubip.ru/schedule/",
    )

class IUBIP(
    private val client: HttpClient,
    private val scope: CoroutineScope
) : Institution {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    companion object Factory : Institution.Factory, KoinComponent {
        private const val BASE_URL = "https://www.iubip.ru"

        override val metadata by lazy { IUBIPMetadata }
        override fun create(): Institution = IUBIP(get(), get())
    }

    override val metadata = Factory.metadata

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
                url = "${BASE_URL}/local/templates/univer/include/schedule/ajax/read-file-groups.php",
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
        
        Auditor.debug(scheduleTag, "Получено дней в расписании: ${if (schedules is ScheduleResponse.Available) schedules.schedules.size else 0}")
        return schedules
    }

    private fun parseWeek(week: JsonElement): List<DaySchedule> {
        val days = mutableListOf<DaySchedule>()

        week.jsonObject.entries.forEach { (dayNumRaw, dayScheduleRaw) ->
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
                url = "${BASE_URL}/local/templates/univer/include/schedule/ajax/read-file-groups.php",
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

    override suspend fun getNews(page: Int): List<NewListItem> {
        val response = client.get("$BASE_URL/news/?PAGEN_1=$page").bodyAsText()
        val document = Ksoup.parse(response)
        val rawData = document.getElementsByClass("news__item")
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "iubip")
        Auditor.debug(netTag, "Получено новостей: ${rawData.size}")

        val data = rawData.map {
            val url = it.getElementsByTag("a").attr("href")
            val id = url.split("/")[2]
            val bannerUrl = formatImageUrl(
                it.getElementsByClass("news__item-image").attr("style")
                    .let { it.slice(it.indexOf("/")..it.lastIndex - 1) })
            val title = it.getElementsByClass("news__item-name").first()!!.text().trim()
            val description = it.getElementsByClass("news__item-text").first()!!.text().trim()
            val date = it.getElementsByClass("news__item-date").text().let {
                val tmp = it.split(" |").first().split(" ")
                LocalDate.of(tmp[2].toInt(), parseMonth(tmp[1]), tmp[0].toInt())
            }
            val tags = emptyList<NewTag>()

            NewListItem(id, url, bannerUrl, title, description, date, tags)
        }

        Auditor.debug(netTag, "Обработано новостей: ${data.size}")

        return data
    }

    override suspend fun getNewDetail(id: String): NewItem {
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "iubip")
        Auditor.debug(netTag, "Загрузка деталей новости: $id")
        
        val url = "$BASE_URL/news/$id/"
        val response = client.get(url).bodyAsText()
        val document = Ksoup.parse(response)

        val bannerUrl = null
        val title = document.getElementsByTag("h1").text()
        val description = null
        val date = null
        val tags = emptyList<NewTag>()

        val content =
            parseNewContent(document.getElementsByClass("content-block__detail-news").first()!!)

        Auditor.debug(netTag, "Новость успешно загружена: $title")
        return NewItem(id, url, bannerUrl, title, description, tags, date, content)
    }

    private fun parseNewContent(tree: Element): NewContent {
        val list = mutableListOf<NewContent>()
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET)

        tree.children().forEach {
            val contentItem = when {
                it.`is`(".detail-news__text") && it.text().isNotBlank() -> {
                    NewContent.Container.Column(
                        it.html().replace("&nbsp;", "").replace("\"", "")
                            .split("<br>\n<br>", "<br>").mapNotNull {
                                it.trim().takeIf { it.isNotBlank() }
                                    ?.let { NewContent.Item.Text(AnnotatedString.fromHtml(it)) }
                            }
                    )
                }

                it.`is`(".univer-gallery__sliders") -> NewContent.Item.ImageCarousel(
                    it.getElementsByClass("univer-gallery__sliders-top-item").map {
                        formatImageUrl(it.getElementsByTag("img").attr("src"))
                    }
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

    private fun formatImageUrl(url: String): String = BASE_URL + url
}

object IUBIPLessonsSchedule {
    val COMMON = listOf(
        LessonTime(1, LocalTime.of(8, 20), LocalTime.of(9, 50)),
        LessonTime(2, LocalTime.of(10, 0), LocalTime.of(11, 30)),
        LessonTime(3, LocalTime.of(11, 40), LocalTime.of(13, 10)),
        LessonTime(4, LocalTime.of(13, 30), LocalTime.of(15, 0)),
        LessonTime(5, LocalTime.of(15, 10), LocalTime.of(16, 40)),
        LessonTime(6, LocalTime.of(17, 0), LocalTime.of(18, 30)),
        LessonTime(7, LocalTime.of(18, 40), LocalTime.of(20, 10)),
        LessonTime(8, LocalTime.of(20, 20), LocalTime.of(21, 50))
    )
}
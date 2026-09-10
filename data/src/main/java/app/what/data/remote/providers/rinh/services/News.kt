package app.what.data.remote.providers.rinh.services

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import app.what.schedule.core.utils.parseMonth
import app.what.domain.models.NewContent
import app.what.domain.models.NewItem
import app.what.domain.models.NewListItem
import app.what.domain.models.NewTag
import app.what.schedule.data.remote.api.NewsService
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class RINHNewsService(
    private val baseUrl: String = "https://rsue.ru",
    private val client: HttpClient
) : NewsService {

    override suspend fun getNews(page: Int): List<NewListItem> {
        val response = client.get("$baseUrl/universitet/novosti/?PAGEN_2=$page").bodyAsText()
        val document = Ksoup.parse(response)
        val rawData = document.getElementsByClass("news-item")

        return rawData.mapNotNull { element ->
            val link = element.getElementsByTag("a").firstOrNull() ?: return@mapNotNull null
            val url = link.attr("href")
            val id = url.split("=").lastOrNull() ?: return@mapNotNull null
            val bannerUrl = formatImageUrl(element.getElementsByTag("img").attr("src"))
            val title = link.text().trim()
            val description = null
            val date = try {
                val dateText = element.getElementById("news-date")?.text() ?: ""
                val tmp = dateText.split(" ")
                LocalDate(tmp[2].toInt(), parseMonth(tmp[1]), tmp[0].toInt())
            } catch (_: Exception) {
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
            val tags = emptyList<NewTag>()

            NewListItem(id, url, bannerUrl, title, description, date, tags)
        }
    }

    override suspend fun getNewDetail(id: String): NewItem {
        val url = "$baseUrl/universitet/novosti/novosti.php?ELEMENT_ID=$id"
        val response = client.get(url).bodyAsText()
        val document = Ksoup.parse(response)

        val bannerUrl = formatImageUrl(document.getElementsByTag("img").attr("src"))
        val title = document.getElementsByTag("h1").firstOrNull()?.text() ?: ""
        val description = null
        val date = try {
            val dateText = document.getElementById("date-news")?.text() ?: ""
            val tmp = dateText.split(" ")
            LocalDate(tmp[2].toInt(), parseMonth(tmp[1]), tmp[0].toInt())
        } catch (_: Exception) {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
        val tags = emptyList<NewTag>()

        val textElem = document.getElementById("text-news")
            ?: document.getElementsByClass("news-detail").firstOrNull()
            ?: document.body()

        val parsedMain = parseNewContent(textElem)
        val sliderElem = document.getElementsByClass("slider-news").firstOrNull()
        val content = if (sliderElem != null) {
            parsedMain.then(parseNewContent(sliderElem))
        } else parsedMain

        return NewItem(id, url, bannerUrl.takeIf { it.isNotBlank() }, title, description, tags, date, content)
    }

    private fun parseNewContent(tree: Element): NewContent {
        val list = mutableListOf<NewContent>()

        tree.children().forEach {
            val contentItem = when {
                it.`is`("p") && it.text().isNotBlank() ->
                    NewContent.Item.Text(AnnotatedString.fromHtml(it.html()))

                it.`is`(".owl-carousel") || it.`is`(".slider-news") ->
                    NewContent.Item.ImageCarousel(
                        it.getElementsByTag("img").map { img -> formatImageUrl(img.attr("src")) }
                    )

                it.`is`("ul") -> NewContent.Item.UnsortedList(
                    it.getElementsByTag("li").map { li -> li.text() })

                it.`is`("ol") -> NewContent.Item.SortedList(
                    it.getElementsByTag("li").map { li -> li.text() })

                else -> null
            }

            if (contentItem != null) {
                list.add(contentItem)
            }
        }

        return NewContent.Container.Column(list)
    }

    private fun formatImageUrl(url: String): String = when {
        url.isEmpty() -> ""
        url.startsWith("http") -> url
        url.startsWith("//") -> "https:$url"
        url.startsWith("/") -> "$baseUrl$url"
        else -> "$baseUrl/$url"
    }
}
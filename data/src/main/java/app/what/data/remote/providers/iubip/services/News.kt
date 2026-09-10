package app.what.data.remote.providers.iubip.services

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

class IUBIPNewsService(
    private val baseUrl: String = "https://www.iubip.ru",
    private val client: HttpClient
) : NewsService {

    override suspend fun getNews(page: Int): List<NewListItem> {
        val response = client.get("$baseUrl/news/?PAGEN_1=$page").bodyAsText()
        val document = Ksoup.parse(response)
        val rawData = document.getElementsByClass("news__item")

        return rawData.mapNotNull { element ->
            val link = element.getElementsByTag("a").firstOrNull() ?: return@mapNotNull null
            val url = link.attr("href")
            val id = url.trim('/').split("/").lastOrNull() ?: return@mapNotNull null
            val style = element.getElementsByClass("news__item-image").firstOrNull()?.attr("style") ?: ""
            val bannerUrl = if (style.contains("/")) {
                val path = style.slice(style.indexOf("/")..<style.lastIndex)
                formatImageUrl(path)
            } else ""

            val title = element.getElementsByClass("news__item-name").firstOrNull()?.text()?.trim() ?: ""
            val description = element.getElementsByClass("news__item-text").firstOrNull()?.text()?.trim()
            val date = try {
                val dateStr = element.getElementsByClass("news__item-date").firstOrNull()?.text() ?: ""
                val tmp = dateStr.split(" |").first().split(" ")
                LocalDate(tmp[2].toInt(), parseMonth(tmp[1]), tmp[0].toInt())
            } catch (_: Exception) {
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
            val tags = emptyList<NewTag>()

            NewListItem(id, url, bannerUrl, title, description, date, tags)
        }
    }

    override suspend fun getNewDetail(id: String): NewItem {
        val url = "$baseUrl/news/$id/"
        val response = client.get(url).bodyAsText()
        val document = Ksoup.parse(response)

        val title = document.getElementsByTag("h1").firstOrNull()?.text() ?: ""
        val detailElem = document.getElementsByClass("content-block__detail-news").firstOrNull()
            ?: document.getElementsByClass("news-detail").firstOrNull()
            ?: document.body()

        val content = parseNewContent(detailElem)

        return NewItem(
            id = id,
            url = url,
            bannerUrl = null,
            title = title,
            description = null,
            tags = emptyList(),
            timestamp = null,
            content = content
        )
    }

    private fun parseNewContent(tree: Element): NewContent {
        val list = mutableListOf<NewContent>()

        tree.children().forEach {
            val contentItem = when {
                it.`is`(".detail-news__text") && it.text().isNotBlank() -> {
                    NewContent.Container.Column(
                        it.html().replace("&nbsp;", " ").replace("\"", "")
                            .split("<br>\n<br>", "<br>").mapNotNull { line ->
                                line.trim().takeIf { str -> str.isNotBlank() }
                                    ?.let { str ->
                                        NewContent.Item.Text(
                                            try {
                                                AnnotatedString.fromHtml(str)
                                            } catch (_: Exception) {
                                                AnnotatedString(str)
                                            }
                                        )
                                    }
                            }
                    )
                }

                it.`is`(".univer-gallery__sliders") ->
                    NewContent.Item.ImageCarousel(
                        it.getElementsByClass("univer-gallery__sliders-top-item").map { item ->
                            formatImageUrl(item.getElementsByTag("img").attr("src"))
                        }
                    )

                it.`is`("ul") -> NewContent.Item.UnsortedList(
                    it.getElementsByTag("li").map { li -> li.text() })

                it.`is`("ol") -> NewContent.Item.SortedList(
                    it.getElementsByTag("li").map { li -> li.text() })

                it.`is`("p") && it.text().isNotBlank() ->
                    NewContent.Item.Text(
                        try {
                            AnnotatedString.fromHtml(it.html())
                        } catch (_: Exception) {
                            AnnotatedString(it.text())
                        }
                    )

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
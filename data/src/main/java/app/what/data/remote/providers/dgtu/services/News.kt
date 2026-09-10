package app.what.data.remote.providers.dgtu.services

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import app.what.domain.models.AuthorInfo
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
import java.time.LocalDate

class DGTUNewsService(
    private val baseUrl: String = "https://donstu.ru",
    private val client: HttpClient
) : NewsService {

    override suspend fun getNews(page: Int): List<NewListItem> {
        val url = "$baseUrl/news/?PAGEN_2=$page"
        val response = client.get(url).bodyAsText()
        val document = Ksoup.parse(response)
        val rawData = document.getElementsByClass("news-card")

        return rawData.mapNotNull { element ->
            val link = element.getElementsByTag("a").firstOrNull() ?: return@mapNotNull null
            val urlPath = link.attr("href")
            val id = urlPath.trim('/').split("/").lastOrNull() ?: return@mapNotNull null
            val bannerUrl = formatImageUrl(element.getElementsByTag("img").attr("src"))
            val title = element.getElementsByTag("h4").firstOrNull()?.text()?.trim() ?: ""
            val description = null
            val date = try {
                val tmp = element.getElementsByTag("time").attr("datetime")
                    .split(" ").first().split(".").map(String::toInt)
                LocalDate.of(tmp[2], tmp[1], tmp[0])
            } catch (_: Exception) {
                LocalDate.now()
            }
            val tags = element.getElementsByClass("tag")
                .map { NewTag(it.text(), it.attr("href").split("=").last()) }

            NewListItem(id, urlPath, bannerUrl, title, description, date, tags)
        }
    }

    override suspend fun getNewDetail(id: String): NewItem {
        val url = "$baseUrl/news/$id"
        val response = client.get(url).bodyAsText()
        val document = Ksoup.parse(response)

        val bannerUrl = formatImageUrl(document.getElementsByTag("img").attr("src"))
        val title = document.getElementsByTag("h1").text().split(" ").dropLast(1).joinToString(" ")
        val description = document.getElementsByClass("detail-hero__subtitle").firstOrNull()?.html()
        val date = try {
            val tmp = document.getElementsByTag("time").attr("datetime")
                .split(" ").first().split(".").map(String::toInt)
            LocalDate.of(tmp[2], tmp[1], tmp[0])
        } catch (_: Exception) {
            LocalDate.now()
        }
        val tags = document.getElementsByClass("detail-hero__card").firstOrNull()
            ?.getElementsByClass("tag")
            ?.map { NewTag(it.text(), it.attr("href").split("=").last()) }
            ?: emptyList()

        val textContentElem = document.selectFirst("div.app-section._gutter-md.container._md.text-content")
            ?: document.getElementsByClass("news-detail").firstOrNull()
            ?: document.getElementsByTag("article").firstOrNull()

        val content = if (textContentElem != null) parseNewContent(textContentElem)
        else NewContent.Container.Column(emptyList())

        return NewItem(
            id = id,
            url = url,
            bannerUrl = bannerUrl.takeIf { it.isNotBlank() },
            title = title.ifBlank { document.getElementsByTag("h1").text() },
            description = description?.let { AnnotatedString.fromHtml(it).takeIf { str -> str.isNotBlank() } },
            tags = tags,
            timestamp = date,
            content = content
        )
    }

    private fun parseNewContent(tree: Element): NewContent {
        val list = mutableListOf<NewContent>()

        tree.children().forEach {
            val contentItem = when {
                it.`is`("p") && it.text().isNotBlank() ->
                    NewContent.Item.Text(AnnotatedString.fromHtml(it.html()))

                it.`is`("section") && it.getElementsByClass("gallery").isNotEmpty() ->
                    NewContent.Item.ImageCarousel(
                        it.getElementsByClass("gallery__thumbs-item").map { img ->
                            formatImageUrl(img.getElementsByTag("img").attr("src"))
                        }
                    )

                it.`is`("blockquote") -> NewContent.Item.Quote(
                    author = AuthorInfo(
                        it.selectFirst(".blockqoute__img")
                            ?.getElementsByTag("img")
                            ?.attr("src")
                            ?.let { src -> formatImageUrl(src) },
                        it.getElementsByClass("blockqoute__author-name").text(),
                        it.getElementsByClass("blockqoute__author-post").text()
                    ),
                    data = it.getElementsByClass("blockqoute__content").firstOrNull()
                        ?.getElementsByTag("p")?.firstOrNull()?.text() ?: it.text()
                )

                it.`is`(".highlight") -> NewContent.Item.Info(
                    it.getElementsByClass("highlight__content").firstOrNull()
                        ?.getElementsByTag("p")?.firstOrNull()?.text() ?: it.text()
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
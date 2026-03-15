package app.what.schedule.data.remote.providers.dgtu.services

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.schedule.data.remote.api.NewsService
import app.what.schedule.data.remote.api.models.AuthorInfo
import app.what.schedule.data.remote.api.models.NewContent
import app.what.schedule.data.remote.api.models.NewItem
import app.what.schedule.data.remote.api.models.NewListItem
import app.what.schedule.data.remote.api.models.NewTag
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import java.time.LocalDate

class DGTUNewsService(
    private val baseUrl: String,
    private val client: HttpClient
) : NewsService {
    override suspend fun getNews(page: Int): List<NewListItem> {
        val url = "$baseUrl/news/?PAGEN_2=$page"
        val response = client.get(url).bodyAsText()
        val document = Ksoup.parse(response)
        val rawData = document.getElementsByClass("news-card")
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "dgtu")
        Auditor.debug(netTag, "Получено новостей: ${rawData.size}")

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

        Auditor.debug(netTag, "Обработано новостей: ${data.size}")

        return data
    }

    override suspend fun getNewDetail(id: String): NewItem {
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "dgtu")
        Auditor.debug(netTag, "Загрузка деталей новости: $id")

        val url = "$baseUrl/news/?PAGEN_2=$id"
        val response = client.get("$baseUrl/news/$id").bodyAsText()
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

        Auditor.debug(netTag, "Новость успешно загружена: $title")
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

    private fun formatImageUrl(url: String): String = baseUrl + url
}
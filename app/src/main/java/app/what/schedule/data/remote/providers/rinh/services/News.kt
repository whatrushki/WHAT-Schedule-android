package app.what.schedule.data.remote.providers.rinh.services

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.schedule.data.remote.api.NewsService
import app.what.schedule.data.remote.api.models.NewContent
import app.what.schedule.data.remote.api.models.NewItem
import app.what.schedule.data.remote.api.models.NewListItem
import app.what.schedule.data.remote.api.models.NewTag
import app.what.schedule.data.remote.utils.parseMonth
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import java.time.LocalDate

class RINHNewsService(
    private val baseUrl: String,
    private val client: HttpClient
) : NewsService {
    override suspend fun getNews(page: Int): List<NewListItem> {
        val response = client.get("$baseUrl/universitet/novosti/?PAGEN_2=$page").bodyAsText()
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

        val url = "$baseUrl/universitet/novosti/novosti.php?ELEMENT_ID=$id"
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

    private fun formatImageUrl(url: String): String = baseUrl + url
}
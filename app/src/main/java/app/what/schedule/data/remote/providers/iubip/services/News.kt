package app.what.schedule.data.remote.providers.iubip.services

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import app.what.foundation.services.AppLogger
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

class IUBIPNewsService(
    private val baseUrl: String,
    private val client: HttpClient
) : NewsService {

    override suspend fun getNews(page: Int): List<NewListItem> {
        val response = client.get("$baseUrl/news/?PAGEN_1=$page").bodyAsText()
        val document = Ksoup.parse(response)
        val rawData = document.getElementsByClass("news__item")
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "iubip")
        AppLogger.Companion.Auditor.debug(netTag, "Получено новостей: ${rawData.size}")

        val data = rawData.map {
            val url = it.getElementsByTag("a").attr("href")
            val id = url.split("/")[2]
            val bannerUrl = formatImageUrl(
                it.getElementsByClass("news__item-image").attr("style")
                    .let { it.slice(it.indexOf("/")..<it.lastIndex) })
            val title = it.getElementsByClass("news__item-name").first()!!.text().trim()
            val description = it.getElementsByClass("news__item-text").first()!!.text().trim()
            val date = it.getElementsByClass("news__item-date").text().let {
                val tmp = it.split(" |").first().split(" ")
                LocalDate.of(tmp[2].toInt(), parseMonth(tmp[1]), tmp[0].toInt())
            }
            val tags = emptyList<NewTag>()

            NewListItem(id, url, bannerUrl, title, description, date, tags)
        }

        AppLogger.Companion.Auditor.debug(netTag, "Обработано новостей: ${data.size}")

        return data
    }

    override suspend fun getNewDetail(id: String): NewItem {
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "iubip")
        AppLogger.Companion.Auditor.debug(netTag, "Загрузка деталей новости: $id")

        val url = "$baseUrl/news/$id/"
        val response = client.get(url).bodyAsText()
        val document = Ksoup.parse(response)

        val bannerUrl = null
        val title = document.getElementsByTag("h1").text()
        val description = null
        val date = null
        val tags = emptyList<NewTag>()

        val content =
            parseNewContent(document.getElementsByClass("content-block__detail-news").first()!!)

        AppLogger.Companion.Auditor.debug(netTag, "Новость успешно загружена: $title")
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
                                    ?.let {
                                        NewContent.Item.Text(
                                            AnnotatedString.Companion.fromHtml(
                                                it
                                            )
                                        )
                                    }
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

    private fun formatImageUrl(url: String): String = baseUrl + url
}
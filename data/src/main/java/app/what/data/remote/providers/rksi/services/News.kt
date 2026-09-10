package app.what.data.remote.providers.rksi.services

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.capitalize as capitalizeFirstChar
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.intl.Locale as UiLocale
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

class RKSINewsService(
    private val baseUrl: String = "https://www.rksi.ru",
    private val client: HttpClient
) : NewsService {

    override suspend fun getNews(page: Int): List<NewListItem> {
        val response = client.get("$baseUrl/news/$page").bodyAsText()
        val document = Ksoup.parse(response)
        val rawData = document.getElementsByClass("flexnews")

        return rawData.mapNotNull { element ->
            val link = element.getElementsByTag("a").firstOrNull() ?: return@mapNotNull null
            val url = baseUrl + link.attr("href")
            val id = url.split("_").lastOrNull() ?: return@mapNotNull null
            val bannerUrl = formatImageUrl(element.getElementsByTag("img").attr("src"))
            val title = element.getElementsByTag("h4").firstOrNull()?.text() ?: ""
            val fullDesc = element.getElementsByTag("div").firstOrNull()?.text() ?: ""
            val description = if (fullDesc.length > title.length + 1) {
                fullDesc.substring(title.length).trim()
            } else fullDesc

            val date = try {
                val spanText = element.getElementsByTag("span").firstOrNull()?.text() ?: ""
                val tmp = spanText.split(".").map(String::toInt)
                LocalDate.of(tmp[2], tmp[1], tmp[0])
            } catch (_: Exception) {
                LocalDate.now()
            }
            val tags = emptyList<NewTag>()

            NewListItem(id, url, bannerUrl, title, description, date, tags)
        }
    }

    private fun <T : Any?> T.addTo(list: MutableList<T>) = list.add(this)

    override suspend fun getNewDetail(id: String): NewItem {
        val url = "$baseUrl/news/n_$id"
        val response = client.get(url).bodyAsText()
        val document = Ksoup.parse(response)

        val bannerUrl = formatImageUrl(document.getElementsByTag("img").attr("src"))
        val title = document.getElementsByTag("h1").text().split(" ").dropLast(1).joinToString(" ")
        val description = document.getElementsByTag("b").firstOrNull()?.html()
        val date = try {
            val dateStr = document.getElementsByTag("h1").text().split(" ").last().drop(1).dropLast(1)
            val tmp = dateStr.split(".").map(String::toInt)
            LocalDate.of(tmp[2], tmp[1], tmp[0])
        } catch (_: Exception) {
            LocalDate.now()
        }

        val mainElem = document.getElementsByTag("main").firstOrNull()
            ?: document.getElementsByTag("article").firstOrNull()
            ?: document.body()

        val content = parseNewContent(mainElem)

        return NewItem(
            id = id,
            url = url,
            bannerUrl = bannerUrl.takeIf { it.isNotBlank() },
            title = title.ifBlank { document.getElementsByTag("h1").text() },
            description = description?.let { AnnotatedString.fromHtml(it).takeIf { str -> str.isNotBlank() } },
            tags = emptyList(),
            timestamp = date,
            content = content
        )
    }

    private fun parseNewContent(tree: Element): NewContent {
        val list = mutableListOf<NewContent>()

        val children = if (tree.children().size > 1) tree.children().drop(1) else tree.children()
        children.forEach {
            when {
                it.`is`("h3") || it.`is`("h2") ->
                    NewContent.Item.Subtitle(it.text()).addTo(list)

                it.`is`("p") && it.getElementsByTag("img").isNotEmpty() -> {
                    it.getElementsByTag("img").forEach { img ->
                        val src = formatImageUrl(img.attr("src"))
                        if (src.isNotBlank()) NewContent.Item.Image(src).addTo(list)
                    }
                }

                it.`is`("p") && it.text().isNotBlank() ->
                    NewContent.Item.Text(AnnotatedString.fromHtml(it.html())).addTo(list)

                it.`is`(".img50") -> it.getElementsByTag("p").forEach { p ->
                    val style = p.attr("style")
                    if ("background-image" in style) {
                        val src = style.substringAfter("'").substringBeforeLast("'")
                        NewContent.Item.Image(formatImageUrl(src)).addTo(list)
                    } else {
                        val img = p.getElementsByTag("img").firstOrNull()
                        if (img != null) {
                            NewContent.Item.Image(formatImageUrl(img.attr("src"))).addTo(list)
                        }
                    }
                }

                it.`is`("ul") -> NewContent.Item.UnsortedList(
                    it.getElementsByTag("li").map { li ->
                        li.text().capitalizeFirstChar(UiLocale.current)
                    }).addTo(list)

                it.`is`("ol") -> NewContent.Item.SortedList(
                    it.getElementsByTag("li").map { li ->
                        li.text().capitalizeFirstChar(UiLocale.current)
                    }).addTo(list)

                it.`is`(".video-container") -> {
                    val iframe = it.getElementsByTag("iframe").attr("src")
                    if (iframe.isNotBlank()) {
                        NewContent.Item.Video.VK(iframe).addTo(list)
                    }
                }
            }
        }

        val images = mutableListOf<String>()
        for (i in list.indices.reversed()) {
            val item = list[i]
            if (item is NewContent.Item.Image) {
                item.data.addTo(images)
                list.removeAt(i)
            } else break
        }

        if (images.size > 1) {
            list.add(NewContent.Item.ImageCarousel(images.reversed()))
        } else if (images.size == 1) {
            list.add(NewContent.Item.Image(images.first()))
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
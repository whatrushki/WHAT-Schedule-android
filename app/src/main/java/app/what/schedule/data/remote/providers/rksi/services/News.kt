package app.what.schedule.data.remote.providers.rksi.services

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.schedule.data.remote.api.NewsService
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
import androidx.compose.ui.text.capitalize as capitalizeFirstChar
import androidx.compose.ui.text.intl.Locale as UiLocale


class RKSINewsService(
    private val baseUrl: String,
    private val client: HttpClient
) : NewsService {
    
    override suspend fun getNews(page: Int): List<NewListItem> {
        val response = client.get("$baseUrl/news/$page").bodyAsText()
        val document = Ksoup.parse(response)
        val rawData = document.getElementsByClass("flexnews")
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "rksi")
        Auditor.debug(netTag, "Получено новостей: ${rawData.size}")
        
        val data = rawData.map {
            val url = baseUrl + it.getElementsByTag("a").attr("href")
            val id = url.split("_").last()
            val bannerUrl = formatImageUrl(it.getElementsByTag("img").attr("src"))
            val title = it.getElementsByTag("h4").first()!!.text()
            val description = it.getElementsByTag("div").first()!!.text()
                .let { it.slice(it.indexOf(" ") + title.length + 1..it.lastIndex) }
            val date = it.getElementsByTag("span").first()!!.text().let {
                val tmp = it.split(".").map(String::toInt)
                LocalDate.of(tmp[2], tmp[1], tmp[0])
            }
            val tags = emptyList<NewTag>()
            
            NewListItem(id, url, bannerUrl, title, description, date, tags)
        }
        
        Auditor.debug(netTag, "Обработано новостей: ${data.size}")
        
        return data
    }
    
    private fun <T : Any?> T.addTo(list: MutableList<T>) = list.add(this)
    
    override suspend fun getNewDetail(id: String): NewItem {
        val url = "$baseUrl/news/n_$id"
        val response = client.get(url).bodyAsText()
        val document = Ksoup.parse(response)
        
        val bannerUrl = formatImageUrl(document.getElementsByTag("img").attr("src"))
        val title = document.getElementsByTag("h1").text().split(" ").dropLast(1).joinToString(" ")
        val description = document.getElementsByTag("b").html()
        val date = document.getElementsByTag("h1").text()
            .split(" ").last().drop(1).dropLast(1).let {
                val tmp = it.split(".").map(String::toInt)
                LocalDate.of(tmp[2], tmp[1], tmp[0])
            }
        val content = parseNewContent(document.getElementsByTag("main").first()!!)
        
        return NewItem(
            id,
            url,
            bannerUrl,
            title,
            AnnotatedString
                .fromHtml(description)
                .takeIf { it.isNotBlank() },
            tags = emptyList(),
            date,
            content
        )
    }
    
    private fun parseNewContent(tree: Element): NewContent {
        val list = mutableListOf<NewContent>()
        
        tree.children().drop(1).forEach {
            
            when {
                it.`is`("h3") -> NewContent.Item.Subtitle(it.text()).addTo(list)
                it.`is`("p") && it.getElementsByTag("img").isNotEmpty() ->
                    NewContent.Item.Image(it.getElementsByTag("img").attr("src")).addTo(list)
                
                it.`is`("p") && it.text()
                    .isNotBlank() -> NewContent.Item.Text(AnnotatedString.fromHtml(it.html()))
                    .addTo(list)
                
                it.`is`(".img50") -> it.getElementsByTag("p").forEach {
                    val style = it.attr("style")
                    if ("background-image" in style) {
                        NewContent.Item.Image(style.substringAfter("'").substringBeforeLast("'"))
                            .addTo(list)
                    } else {
                        NewContent.Item.Image(it.getElementsByTag("img")[0].attr("src")).addTo(list)
                    }
                    
                }
                
                it.`is`("ul") -> NewContent.Item.UnsortedList(
                    it.getElementsByTag("li").map {
                        it.text().capitalizeFirstChar(UiLocale.current)
                    }).addTo(list)
                
                it.`is`("ol") -> NewContent.Item.SortedList(
                    it.getElementsByTag("li")
                        .map { it.text().capitalizeFirstChar(UiLocale.current) }).addTo(list)
                
                it.`is`(".video-container") ->
                    NewContent.Item.Video.VK(it.getElementsByTag("iframe").attr("src"))
                        .addTo(list)
            }
        }
        val images = mutableListOf<String>()
        for (i in list.indices.reversed()) {
            val it = list[i]
            if (it is NewContent.Item.Image) {
                it.data.addTo(images)
                list.removeAt(i)
            } else break
        }
        
        if (images.isNotEmpty())
            list.add(NewContent.Item.ImageCarousel(images))
        
        return NewContent.Container.Column(list)
    }
    
    private fun formatImageUrl(url: String): String = baseUrl + url
}
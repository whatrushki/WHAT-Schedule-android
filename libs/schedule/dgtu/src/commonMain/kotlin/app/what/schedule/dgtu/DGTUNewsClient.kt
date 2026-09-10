package app.what.schedule.dgtu

import app.what.schedule.core.clients.NewsClient
import app.what.schedule.core.models.NewDetailDto
import app.what.schedule.core.models.NewListItemDto
import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DGTUNewsClient(
    private val client: HttpClient,
    private val baseUrl: String = "https://donstu.ru"
) : NewsClient {

    private fun formatImageUrl(url: String): String = when {
        url.isEmpty() -> ""
        url.startsWith("http") -> url
        url.startsWith("//") -> "https:$url"
        url.startsWith("/") -> "$baseUrl$url"
        else -> "$baseUrl/$url"
    }

    override suspend fun getNews(page: Int): List<NewListItemDto> {
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

            val dateText = element.getElementsByTag("time").attr("datetime")
            val date = try {
                val tmp = dateText.split(" ").first().split(".").map { it.trim().toInt() }
                LocalDate(tmp[2], tmp[1], tmp[0])
            } catch (_: Exception) {
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            }

            NewListItemDto(
                id = id,
                title = title,
                description = "",
                date = date,
                imageUrl = bannerUrl.takeIf { it.isNotBlank() },
                sourceUrl = if (urlPath.startsWith("http")) urlPath else "$baseUrl$urlPath"
            )
        }
    }

    override suspend fun getNewDetail(id: String): NewDetailDto {
        val url = "$baseUrl/news/$id"
        val response = client.get(url).bodyAsText()
        val document = Ksoup.parse(response)

        val title = document.getElementsByTag("h1").firstOrNull()?.text()?.trim() ?: ""
        val contentElement = document.getElementsByClass("news-detail").firstOrNull()
            ?: document.getElementsByTag("article").firstOrNull()
        val fullText = contentElement?.text()?.trim() ?: ""
        val images = contentElement?.getElementsByTag("img")?.mapNotNull {
            val src = it.attr("src")
            if (src.isNotBlank()) formatImageUrl(src) else null
        } ?: emptyList()

        return NewDetailDto(
            id = id,
            title = title,
            fullText = fullText,
            date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
            images = images,
            sourceUrl = url
        )
    }
}

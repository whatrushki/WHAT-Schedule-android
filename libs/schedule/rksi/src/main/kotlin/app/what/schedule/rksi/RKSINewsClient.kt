package app.what.schedule.rksi

import app.what.schedule.core.clients.NewsClient
import app.what.schedule.core.models.NewDetailDto
import app.what.schedule.core.models.NewListItemDto
import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import java.time.LocalDate

class RKSINewsClient(
    private val client: HttpClient,
    private val baseUrl: String = "https://www.rksi.ru"
) : NewsClient {

    private fun formatImageUrl(url: String): String = when {
        url.isEmpty() -> ""
        url.startsWith("http") -> url
        url.startsWith("//") -> "https:$url"
        url.startsWith("/") -> "$baseUrl$url"
        else -> "$baseUrl/$url"
    }

    override suspend fun getNews(page: Int): List<NewListItemDto> {
        val response = client.get("$baseUrl/news/$page").bodyAsText()
        val document = Ksoup.parse(response)
        val rawData = document.getElementsByClass("flexnews")

        return rawData.mapNotNull { element ->
            val link = element.getElementsByTag("a").firstOrNull() ?: return@mapNotNull null
            val url = baseUrl + link.attr("href")
            val id = url.split("_").lastOrNull() ?: return@mapNotNull null
            val bannerUrl = formatImageUrl(element.getElementsByTag("img").attr("src"))
            val title = element.getElementsByTag("h4").firstOrNull()?.text()?.trim() ?: ""
            val fullDesc = element.getElementsByTag("div").firstOrNull()?.text()?.trim() ?: ""
            val description = if (fullDesc.length > title.length) fullDesc.removePrefix(title).trim() else fullDesc

            val dateText = element.getElementsByTag("span").firstOrNull()?.text()?.trim() ?: ""
            val date = try {
                val tmp = dateText.split(".").map { it.trim().toInt() }
                LocalDate.of(tmp[2], tmp[1], tmp[0])
            } catch (_: Exception) {
                LocalDate.now()
            }

            NewListItemDto(
                id = id,
                title = title,
                description = description,
                date = date,
                imageUrl = bannerUrl.takeIf { it.isNotBlank() },
                sourceUrl = url
            )
        }
    }

    override suspend fun getNewDetail(id: String): NewDetailDto {
        val url = "$baseUrl/news/n_$id"
        val response = client.get(url).bodyAsText()
        val document = Ksoup.parse(response)

        val titleRaw = document.getElementsByTag("h1").firstOrNull()?.text()?.trim() ?: ""
        val title = titleRaw.split(" ").dropLast(1).joinToString(" ")
        val dateText = titleRaw.split(" ").lastOrNull()?.trim('(', ')') ?: ""
        val date = try {
            val tmp = dateText.split(".").map { it.trim().toInt() }
            LocalDate.of(tmp[2], tmp[1], tmp[0])
        } catch (_: Exception) {
            LocalDate.now()
        }

        val mainElement = document.getElementsByTag("main").firstOrNull()
        val fullText = mainElement?.text()?.trim() ?: ""
        val images = mainElement?.getElementsByTag("img")?.mapNotNull {
            val src = it.attr("src")
            if (src.isNotBlank()) formatImageUrl(src) else null
        } ?: emptyList()

        return NewDetailDto(
            id = id,
            title = title.ifEmpty { titleRaw },
            fullText = fullText,
            date = date,
            images = images,
            sourceUrl = url
        )
    }
}

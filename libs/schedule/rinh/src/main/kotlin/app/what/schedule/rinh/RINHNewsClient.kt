package app.what.schedule.rinh

import app.what.schedule.core.clients.NewsClient
import app.what.schedule.core.models.NewDetailDto
import app.what.schedule.core.models.NewListItemDto
import app.what.schedule.core.utils.parseMonth
import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import java.time.LocalDate

class RINHNewsClient(
    private val client: HttpClient,
    private val baseUrl: String = "https://rsue.ru",
    private val log: ((String) -> Unit)? = null
) : NewsClient {

    override suspend fun getNews(page: Int): List<NewListItemDto> {
        return try {
            val response = client.get("$baseUrl/universitet/novosti/?PAGEN_2=$page").bodyAsText()
            val document = Ksoup.parse(response)
            val rawData = document.getElementsByClass("news-item")

            rawData.mapNotNull {
                try {
                    val anchor = it.getElementsByTag("a").firstOrNull() ?: return@mapNotNull null
                    val url = anchor.attr("href")
                    val id = url.split("=").lastOrNull() ?: url
                    val imgTag = it.getElementsByTag("img").firstOrNull()
                    val bannerUrl = imgTag?.attr("src")?.let { src ->
                        if (src.startsWith("http")) src else baseUrl + src
                    }
                    val title = anchor.text().trim()
                    val dateText = it.getElementById("news-date")?.text()?.trim()
                    val date = try {
                        val tmp = dateText?.split(" ")
                        if (tmp != null && tmp.size >= 3) {
                            LocalDate.of(tmp[2].toInt(), parseMonth(tmp[1]), tmp[0].toInt())
                        } else LocalDate.now()
                    } catch (_: Exception) {
                        LocalDate.now()
                    }

                    NewListItemDto(
                        id = id,
                        title = title,
                        description = "",
                        date = date,
                        imageUrl = bannerUrl,
                        sourceUrl = if (url.startsWith("http")) url else baseUrl + url
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            log?.invoke("Ошибка получения новостей РГЭУ РИНХ: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getNewDetail(id: String): NewDetailDto {
        val url = "$baseUrl/universitet/novosti/novosti.php?ELEMENT_ID=$id"
        return try {
            val response = client.get(url).bodyAsText()
            val document = Ksoup.parse(response)

            val title = document.getElementsByTag("h1").firstOrNull()?.text()?.trim().orEmpty()
            val imgTag = document.getElementsByTag("img").firstOrNull()
            val bannerUrl = imgTag?.attr("src")?.let { src ->
                if (src.startsWith("http")) src else baseUrl + src
            }

            val dateText = document.getElementById("date-news")?.text()?.trim()
            val date = try {
                val tmp = dateText?.split(" ")
                if (tmp != null && tmp.size >= 3) {
                    LocalDate.of(tmp[2].toInt(), parseMonth(tmp[1]), tmp[0].toInt())
                } else LocalDate.now()
            } catch (_: Exception) {
                LocalDate.now()
            }

            val textElement = document.getElementById("text-news")
            val htmlContent = textElement?.html() ?: ""

            NewDetailDto(
                id = id,
                title = title,
                fullText = htmlContent,
                date = date,
                images = bannerUrl?.let { listOf(it) } ?: emptyList(),
                sourceUrl = url
            )
        } catch (e: Exception) {
            log?.invoke("Ошибка получения новости РГЭУ РИНХ $id: ${e.message}")
            NewDetailDto(
                id = id,
                title = "",
                fullText = "",
                date = LocalDate.now(),
                sourceUrl = url
            )
        }
    }
}

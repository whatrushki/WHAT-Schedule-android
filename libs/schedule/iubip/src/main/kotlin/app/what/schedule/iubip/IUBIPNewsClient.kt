package app.what.schedule.iubip

import app.what.schedule.core.clients.NewsClient
import app.what.schedule.core.models.NewDetailDto
import app.what.schedule.core.models.NewListItemDto
import app.what.schedule.core.utils.parseMonth
import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import java.time.LocalDate

class IUBIPNewsClient(
    private val client: HttpClient,
    private val baseUrl: String = "https://www.iubip.ru",
    private val log: ((String) -> Unit)? = null
) : NewsClient {

    override suspend fun getNews(page: Int): List<NewListItemDto> {
        return try {
            val response = client.get("$baseUrl/news/?PAGEN_1=$page").bodyAsText()
            val document = Ksoup.parse(response)
            val rawData = document.getElementsByClass("news__item")

            rawData.mapNotNull {
                try {
                    val anchor = it.getElementsByTag("a").firstOrNull() ?: return@mapNotNull null
                    val url = anchor.attr("href")
                    val id = url.split("/").getOrNull(2) ?: url
                    val style = it.getElementsByClass("news__item-image").attr("style")
                    val bannerUrl = if ("/" in style) {
                        baseUrl + style.substring(style.indexOf("/"), style.length - 1)
                    } else null

                    val title = it.getElementsByClass("news__item-name").firstOrNull()?.text()?.trim().orEmpty()
                    val description = it.getElementsByClass("news__item-text").firstOrNull()?.text()?.trim().orEmpty()
                    val dateText = it.getElementsByClass("news__item-date").text()
                    val date = try {
                        val tmp = dateText.split(" |").first().split(" ")
                        LocalDate.of(tmp[2].toInt(), parseMonth(tmp[1]), tmp[0].toInt())
                    } catch (_: Exception) {
                        LocalDate.now()
                    }

                    NewListItemDto(
                        id = id,
                        title = title,
                        description = description,
                        date = date,
                        imageUrl = bannerUrl,
                        sourceUrl = if (url.startsWith("http")) url else baseUrl + url
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            log?.invoke("Ошибка получения новостей ИУБиП: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getNewDetail(id: String): NewDetailDto {
        val url = "$baseUrl/news/$id/"
        return try {
            val response = client.get(url).bodyAsText()
            val document = Ksoup.parse(response)

            val title = document.getElementsByTag("h1").text()
            val contentElement = document.getElementsByClass("content-block__detail-news").firstOrNull()
            val htmlContent = contentElement?.html() ?: ""

            NewDetailDto(
                id = id,
                title = title,
                fullText = htmlContent,
                date = LocalDate.now(),
                images = emptyList(),
                sourceUrl = url
            )
        } catch (e: Exception) {
            log?.invoke("Ошибка получения новости ИУБиП $id: ${e.message}")
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

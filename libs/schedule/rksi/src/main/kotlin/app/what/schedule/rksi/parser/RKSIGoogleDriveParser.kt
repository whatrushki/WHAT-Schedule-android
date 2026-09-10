package app.what.schedule.rksi.parser

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime

fun List<RKSIGoogleDriveParser.Item>.files(): List<RKSIGoogleDriveParser.Item.File> =
    filterIsInstance<RKSIGoogleDriveParser.Item.File>()

fun List<RKSIGoogleDriveParser.Item>.folders(): List<RKSIGoogleDriveParser.Item.Folder> =
    filterIsInstance<RKSIGoogleDriveParser.Item.Folder>()

class RKSIGoogleDriveParser(
    private val client: HttpClient,
    private val log: ((String) -> Unit)? = null
) {
    sealed class Item(val id: String, val name: String, val lastModified: LocalDateTime) {
        val additionalData: MutableMap<String, Any> = mutableMapOf()

        class Folder(id: String, name: String, lastModified: LocalDateTime) :
            Item(id, name, lastModified)

        class File(id: String, name: String, lastModified: LocalDateTime) :
            Item(id, name, lastModified) {
            fun getDownloadLink(): String =
                "https://drive.google.com/uc?id=$id&export=download"
        }

        override fun toString(): String =
            "${this::class.simpleName}(id=$id, name=$name, lastModified=$lastModified)"
    }

    suspend fun getFolderContent(folderId: String): List<Item> {
        log?.invoke("Загрузка содержимого папки Google Drive: $folderId")

        var items: List<Item> = emptyList()
        var lastException: Exception? = null

        for (attempt in 1..3) {
            try {
                log?.invoke("Попытка $attempt загрузки папки $folderId")
                val response = client.get("https://drive.google.com/embeddedfolderview?id=$folderId#list") {
                    header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                }.bodyAsText()
                val document = Ksoup.parse(response)

                val entries = document.select(".flip-entry")
                if (entries.isNotEmpty()) {
                    items = entries.mapNotNull { entry ->
                        val link = entry.selectFirst("a[href]") ?: return@mapNotNull null
                        val href = link.attr("href")
                        val id = entry.id().removePrefix("entry-").ifEmpty {
                            href.substringAfterLast("/").substringBefore("?").substringBefore("&")
                        }
                        val name = entry.selectFirst(".flip-entry-title")?.text()?.trim() ?: ""
                        if (name.isEmpty()) return@mapNotNull null

                        val isFolder = href.contains("/folders/") || entry.select(".drive-sprite-folder").isNotEmpty() || '.' !in name
                        val dateText = entry.selectFirst(".flip-entry-last-modified")?.text()?.trim() ?: ""

                        val date = try {
                            if (":" in dateText) {
                                val timePart = dateText.replace("[^0-9:]".toRegex(), "")
                                val raw = timePart.split(":").map(String::toInt)
                                LocalDate.now().atTime(raw.getOrElse(0) { 0 }, raw.getOrElse(1) { 0 })
                            } else {
                                LocalDate.now().atStartOfDay()
                            }
                        } catch (_: Exception) {
                            LocalDate.now().atStartOfDay()
                        }

                        if (isFolder) Item.Folder(id, name, date)
                        else Item.File(id, name, date)
                    }
                } else {
                    // Fallback to legacy structure if present
                    items = document.getElementsByAttributeValue("data-target", "doc").mapNotNull {
                        val id = it.attr("data-id")
                        val name = it.selectFirst("strong")?.text() ?: return@mapNotNull null
                        val date = LocalDate.now().atStartOfDay()
                        if ('.' in name) Item.File(id, name, date)
                        else Item.Folder(id, name, date)
                    }
                }

                if (items.isNotEmpty()) {
                    return items
                }
            } catch (e: Exception) {
                lastException = e
                log?.invoke("Ошибка загрузки папки $folderId: ${e.message}")
            }
            delay(300L * attempt)
        }

        if (items.isEmpty() && lastException != null) {
            log?.invoke("Не удалось получить файлы Google Drive: ${lastException.message}")
        }
        return items
    }
}

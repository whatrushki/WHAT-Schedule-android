package app.what.data.libs

import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.retry
import app.what.foundation.utils.LogCat
import app.what.foundation.utils.LogScope
import app.what.foundation.utils.buildTag
import com.fleeksoft.ksoup.Ksoup
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun List<GoogleDriveParser.Item>.files() =
    filterIsInstance<GoogleDriveParser.Item.File>()

fun List<GoogleDriveParser.Item>.folders() =
    filterIsInstance<GoogleDriveParser.Item.Folder>()

class GoogleDriveParser(
    private val client: HttpClient
) {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    
    companion object {
        private val googleDriveMonths =
            listOf("ян", "фе", "мар", "ап", "май", "июн", "июл", "ав", "се", "ок", "но", "де")
    }
    
    sealed class Item(val id: String, val name: String, val lastModified: LocalDateTime) {
        val additionalData: MutableMap<String, Any> = mutableMapOf()
        
        class Folder(id: String, name: String, lastModified: LocalDateTime) :
            Item(id, name, lastModified)
        
        class File(id: String, name: String, lastModified: LocalDateTime) :
            Item(id, name, lastModified) {
            fun getDownloadLink() =
                "https://drive.usercontent.google.com/uc?id=$id&authuser=0&export=download"
        }
        
        override fun toString(): String {
            return "${this::class.simpleName}(id=$id, name=$name, lastModified=$lastModified)"
        }
    }
    
    suspend fun getFolderContent(folderId: String): List<Item> {
        val netTag = buildTag(LogScope.NETWORK, LogCat.NET, "gdrive")
        Auditor.debug(netTag, "Загрузка содержимого папки Google Drive: $folderId")
        crashlytics.setCustomKey("gdrive_folder_id", folderId)
        
        var items: List<Item> = emptyList()
        try {
            retry(3, 300) { attempt ->
                Auditor.debug(netTag, "Попытка $attempt загрузки содержимого папки")
                val response = client.get("https://drive.google.com/embeddedfolderview?id=$folderId#list").bodyAsText()
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
                    // Fallback to legacy parser if embedded view has no flip-entry
                    items = document.getElementsByAttributeValue("data-target", "doc").mapNotNull {
                        val id = it.attr("data-id")
                        val name = it.selectFirst("strong")?.text() ?: return@mapNotNull null
                        val date = LocalDate.now().atStartOfDay()
                        if ('.' in name) Item.File(id, name, date)
                        else Item.Folder(id, name, date)
                    }
                }
            }
        } catch (e: Exception) {
            Auditor.err(netTag, "Не удалось загрузить содержимое папки $folderId: ${e.message}", e)
            crashlytics.recordException(e)
        }
        
        Auditor.debug(netTag, "Загружено элементов из Google Drive: ${items.size}")
        crashlytics.setCustomKey("gdrive_items_count", items.size)
        val filesCount = items.count { it is Item.File }
        val foldersCount = items.count { it is Item.Folder }
        Auditor.debug(netTag, "Файлов: $filesCount, папок: $foldersCount")
        return items
    }
}

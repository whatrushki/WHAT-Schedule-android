package app.what.schedule.libs

import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.retry
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
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
        retry(5, 200) { attempt ->
            Auditor.debug(netTag, "Попытка $attempt загрузки содержимого папки")
            val response =
                client.get("https://drive.google.com/drive/folders/$folderId").bodyAsText()
            val document = Ksoup.parse(response)
            items = document.getElementsByAttributeValue("data-target", "doc").map {
                val id = it.attr("data-id")
                val name = it.selectFirst("strong")!!.text()
                val date = it.getElementsByAttributeValue("data-column-field", "5").text().let {
                    if (":" in it) {
                        val raw = it.split(":").map(String::toInt)
                        LocalDate.now().atTime(raw[0], raw[1])
                    } else {
                        val raw = it.split(" ")
                        LocalDateTime.of(
                            LocalDate.of(
                                if (raw.getOrNull(2) != null) raw[2].toInt() else LocalDate.now().year,
                                googleDriveMonths.first { it in raw[1] }
                                    .let { googleDriveMonths.indexOf(it) + 1 },
                                raw[0].toInt()
                            ), LocalTime.of(0, 0)
                        )
                    }
                }

                return@map if ('.' in name) Item.File(id, name, date)
                else Item.Folder(id, name, date)
            }
        }

        Auditor.debug(netTag, "Загружено элементов из Google Drive: ${items.size}")
        crashlytics.setCustomKey("gdrive_items_count", items.size)
        val filesCount = items.count { it is Item.File }
        val foldersCount = items.count { it is Item.Folder }
        Auditor.debug(netTag, "Файлов: $filesCount, папок: $foldersCount")
        return items
    }
}

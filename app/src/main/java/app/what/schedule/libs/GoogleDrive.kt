package app.what.schedule.libs

import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.retry
import com.fleeksoft.ksoup.Ksoup
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
        Auditor.debug("d", "getFolderContent: https://drive.google.com/drive/folders/$folderId")
        var items: List<Item> = emptyList()
        retry(5, 200) {
            Auditor.debug("d", "getFolderContent: attempt $it")
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

                Auditor.debug("date", "ddddate: $date")
                return@map if ('.' in name) Item.File(id, name, date)
                else Item.Folder(id, name, date)
            }
        }

        return items
    }
}

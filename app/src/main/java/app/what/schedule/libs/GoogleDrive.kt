package app.what.schedule.libs

import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.retry
import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

fun List<GoogleDriveParser.Item>.files() =
    filterIsInstance<GoogleDriveParser.Item.File>()

fun List<GoogleDriveParser.Item>.folders() =
    filterIsInstance<GoogleDriveParser.Item.Folder>()

class GoogleDriveParser(
    private val client: HttpClient
) {
    sealed class Item(val id: String, val name: String) {
        override fun toString(): String {
            return "${this::class.simpleName}(id=$id, name=$name)"
        }

        class Folder(id: String, name: String) : Item(id, name)
        class File(id: String, name: String) : Item(id, name) {
            fun getDownloadLink() =
                "https://drive.usercontent.google.com/uc?id=$id&authuser=0&export=download"
        }
    }

    suspend fun getFolderContent(folderId: String): List<Item> {
        Auditor.debug("d", "getFolderContent: https://drive.google.com/drive/folders/$folderId")
        var items: List<Item> = emptyList()
        retry(3, 500) {
            Auditor.debug("d", "getFolderContent: attempt $it")
            val response =
                client.get("https://drive.google.com/drive/folders/$folderId").bodyAsText()
            val document = Ksoup.parse(response)
            items = document.getElementsByAttributeValue("data-target", "doc").map {
                val id = it.attr("data-id")
                val name = it.getElementsByClass("KL4NAf").first()!!.text()

                return@map if ('.' in name) Item.File(id, name)
                else Item.Folder(id, name)
            }
        }

        return items
    }
}

package app.what.schedule.data.remote.api.models

import androidx.compose.ui.text.AnnotatedString
import app.what.schedule.data.remote.utils.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class NewListItem(
    val id: String,
    val url: String,
    val bannerUrl: String,
    val title: String,
    val description: String?,
    @Serializable(LocalDateSerializer::class)
    val timestamp: LocalDate,
    val tags: List<NewTag>
)


data class NewItem(
    val id: String,
    val url: String,
    val bannerUrl: String?,
    val title: String,
    val description: AnnotatedString?,
    val tags: List<NewTag>,
    val timestamp: LocalDate?,
    val content: NewContent
)

@Serializable
data class NewTag(
    val name: String,
    val id: String
)

data class AuthorInfo(
    val avatarUrl: String?,
    val name: String,
    val role: String
)

sealed interface NewContent {
    fun isNotEmpty() = !isEmpty()
    fun isEmpty() = when (this) {
        is Item.Text -> data.text.isEmpty()
        is Container -> content.isEmpty()
        // TODO: доделать если понадобится в будущем
        else -> false
    }

    infix fun then(other: NewContent?): NewContent = if (other == null) this else
        Container.Column(listOf(this, other))

    fun toTreeString(): String = buildString {
        appendContent(this@NewContent, 0)
    }

    private fun StringBuilder.appendContent(content: NewContent, indentLevel: Int) {
        val indent = "  ".repeat(indentLevel)

        when (content) {
            is Container -> {
                appendLine("$indent${content::class.simpleName}:")
                content.content.forEach { child ->
                    appendContent(child, indentLevel + 1)
                }
            }

            is Item -> {
                when (content) {
                    is Item.SortedList -> appendLine("$indent• SortedList: ${content.data}")
                    is Item.UnsortedList -> appendLine("$indent• UnsortedList: ${content.data}")
                    is Item.Text -> appendLine("$indent• Text: \"${content.data}\"")
                    is Item.Subtitle -> appendLine("$indent• Subtitle: \"${content.data}\"")
                    is Item.ImageCarousel -> appendLine("$indent• ImageCarousel: ${content.data.size} images")
                    is Item.Image -> appendLine("$indent• Image: ${content.data}")
                    is Item.Table -> appendLine("$indent• Table: ${content.data.size}x${content.data.firstOrNull()?.size ?: 0}")
                    is Item.Info -> appendLine("$indent• Info: ${content.data}")
                    is Item.Quote -> appendLine("$indent• Quote: ${content.author.name} -> ${content.data}")
                    is Item.SimpleText -> appendLine("$indent• Simple Text: \"${content.data}\"")
                }
            }
        }
    }


    sealed class Container(val content: List<NewContent>) : NewContent {
        class Column(content: List<NewContent>) : Container(content)
        class Row(content: List<NewContent>) : Container(content)
        class Card(content: List<NewContent>) : Container(content)
    }

    sealed interface Item : NewContent {
        class Quote(val author: AuthorInfo, val data: String) : Item
        class Info(val data: String) : Item
        class SortedList(val data: List<String>) : Item
        class UnsortedList(val data: List<String>) : Item
        class SimpleText(val data: String) : Item
        class Text(val data: AnnotatedString) : Item
        class Subtitle(val data: String) : Item
        class ImageCarousel(val data: List<String>) : Item
        class Image(val data: String) : Item
        class Table(val data: List<List<String>>) : Item
    }
}
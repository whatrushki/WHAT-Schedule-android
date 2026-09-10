package app.what.domain.models

import androidx.compose.ui.text.AnnotatedString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

object DomainLocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }
}

@Serializable
data class NewListItem(
    val id: String,
    val url: String,
    val bannerUrl: String,
    val title: String,
    val description: String?,
    @Serializable(DomainLocalDateSerializer::class)
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
        else -> false
    }
    
    infix fun then(other: NewContent?): NewContent = if (other == null) this else
        Container.Column(listOf(this, other))
    
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
        sealed class Video(val data: String) : Item {
            class VK(data: String) : Video(data)
        }
    }
}

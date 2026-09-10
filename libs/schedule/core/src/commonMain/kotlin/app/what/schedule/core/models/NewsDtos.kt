package app.what.schedule.core.models

import kotlinx.datetime.LocalDate

data class NewListItemDto(
    val id: String,
    val title: String,
    val description: String,
    val date: LocalDate,
    val imageUrl: String? = null,
    val sourceUrl: String? = null
)

data class NewDetailDto(
    val id: String,
    val title: String,
    val fullText: String,
    val date: LocalDate,
    val images: List<String> = emptyList(),
    val sourceUrl: String? = null
)

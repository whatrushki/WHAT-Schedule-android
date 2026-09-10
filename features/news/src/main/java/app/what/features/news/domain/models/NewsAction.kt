package app.what.schedule.features.news.domain.models

import app.what.domain.models.NewListItem

sealed interface NewsAction {
    data class NavigateToNewsDetail(
        val item: NewListItem
    ) : NewsAction
}
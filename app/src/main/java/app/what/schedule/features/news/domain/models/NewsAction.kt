package app.what.schedule.features.news.domain.models

import app.what.schedule.data.remote.api.models.NewListItem

sealed interface NewsAction {
    data class NavigateToNewsDetail(
        val item: NewListItem
    ) : NewsAction
}
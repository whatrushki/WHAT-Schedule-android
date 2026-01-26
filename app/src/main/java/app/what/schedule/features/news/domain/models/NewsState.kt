package app.what.schedule.features.news.domain.models

import app.what.foundation.data.RemoteState
import app.what.schedule.data.remote.api.models.NewListItem

data class NewsState(
    val page: Int = 1,
    val news: List<NewListItem> = emptyList(),
    val newsState: RemoteState = RemoteState.Idle,
//    val filters: NewsFilter? = null
)



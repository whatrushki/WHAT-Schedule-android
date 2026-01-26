package app.what.schedule.features.newsDetail.domain.models

import app.what.foundation.data.RemoteState
import app.what.schedule.data.remote.api.models.NewItem
import app.what.schedule.data.remote.api.models.NewListItem

data class NewsDetailState(
    val newListInfo: NewListItem,
    val newDetailInfo: NewItem? = null,
    val newState: RemoteState = RemoteState.Idle
)



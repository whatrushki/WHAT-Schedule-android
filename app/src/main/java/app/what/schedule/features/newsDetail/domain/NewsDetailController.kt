package app.what.schedule.features.newsDetail.domain

import androidx.lifecycle.viewModelScope
import app.what.foundation.core.UIController
import app.what.foundation.data.RemoteState
import app.what.foundation.utils.launchSafe
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.remote.api.models.NewListItem
import app.what.schedule.domain.NewsRepository
import app.what.schedule.features.newsDetail.domain.models.NewsDetailAction
import app.what.schedule.features.newsDetail.domain.models.NewsDetailEvent
import app.what.schedule.features.newsDetail.domain.models.NewsDetailState
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class NewsDetailController(
    item: NewListItem,
    private val settings: AppValues
) : UIController<NewsDetailState, NewsDetailAction, NewsDetailEvent>(
    NewsDetailState(item)
), KoinComponent {
    private val apiRepository: NewsRepository by inject()

    override fun obtainEvent(viewEvent: NewsDetailEvent) = when (viewEvent) {
        NewsDetailEvent.Init -> {}
        NewsDetailEvent.OnRefresh -> requestInfo()
    }

    init {
        requestInfo()
    }

    val debugMode: Boolean
        get() = settings.debugMode.get() == true

    private fun requestInfo() {
        updateState { copy(newState = RemoteState.Loading) }

        viewModelScope.launchSafe(
            debug = debugMode,
            onFailure = {
                updateState { copy(newState = RemoteState.Error(it)) }
            }
        ) {
            val data = apiRepository.getNewDetail(viewState.newListInfo.id)

            updateState {
                copy(
                    newState = RemoteState.Success,
                    newDetailInfo = data
                )
            }
        }
    }
}
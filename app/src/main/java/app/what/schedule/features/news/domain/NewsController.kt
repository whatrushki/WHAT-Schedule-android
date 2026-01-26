package app.what.schedule.features.news.domain

import androidx.lifecycle.viewModelScope
import app.what.foundation.core.UIController
import app.what.foundation.data.RemoteState
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.launchSafe
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.remote.api.models.NewListItem
import app.what.schedule.domain.NewsRepository
import app.what.schedule.features.news.domain.models.NewsAction
import app.what.schedule.features.news.domain.models.NewsEvent
import app.what.schedule.features.news.domain.models.NewsState


class NewsController(
    private val apiRepository: NewsRepository,
    private val settings: AppValues
) : UIController<NewsState, NewsAction, NewsEvent>(
    NewsState()
) {
    override fun obtainEvent(viewEvent: NewsEvent) = when (viewEvent) {
        NewsEvent.Init -> {}
        NewsEvent.OnListEndingScrolled -> requestNextPage()
        NewsEvent.OnRefresh -> requestNextPage(true)
        is NewsEvent.OnNewEnterClicked -> selectNew(viewEvent.value)
    }

    init {
        requestNextPage()
    }

    val debugMode: Boolean
        get() = settings.debugMode.get() == true

    private fun selectNew(item: NewListItem) {
        setAction(NewsAction.NavigateToNewsDetail(item))
    }

    private fun requestNextPage(rollback: Boolean = false) {
        Auditor.debug("d", "request next page ${viewState.page}")
        updateState {
            copy(
                newsState = RemoteState.Loading,
                page = if (rollback) 1 else viewState.page
            )
        }

        viewModelScope.launchSafe(
            debug = debugMode, onFailure = {
                updateState { copy(newsState = RemoteState.Error(it)) }
            }
        ) {
            val data = apiRepository.getNews(viewState.page)

            updateState {
                copy(
                    newsState = RemoteState.Success,
                    news = if (rollback) data else viewState.news + data,
                    page = viewState.page + 1
                )
            }
        }
    }
}
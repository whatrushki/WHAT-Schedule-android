package app.what.schedule.features.news.domain

import androidx.lifecycle.viewModelScope
import app.what.foundation.core.UIController
import app.what.foundation.data.RemoteState
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
    private val crashlytics = FirebaseCrashlytics.getInstance()
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
        val newsTag = buildTag(LogScope.NEWS, LogCat.NET)
        val page = if (rollback) 1 else viewState.page
        Auditor.debug(newsTag, "Запрос новостей, страница: $page")
        
        updateState {
            copy(
                newsState = RemoteState.Loading,
                page = page
            )
        }

        viewModelScope.launchSafe(
            debug = debugMode, onFailure = {
                Auditor.err(newsTag, "Ошибка загрузки новостей", it)
                crashlytics.setCustomKey("news_load_error", it.message ?: "unknown")
                crashlytics.recordException(it)
                updateState { copy(newsState = RemoteState.Error(it)) }
            }
        ) {
            val data = apiRepository.getNews(page)
            Auditor.debug(newsTag, "Новости загружены, количество: ${data.size}")

            updateState {
                copy(
                    newsState = RemoteState.Success,
                    news = if (rollback) data else viewState.news + data,
                    page = page + 1
                )
            }
        }
    }
}
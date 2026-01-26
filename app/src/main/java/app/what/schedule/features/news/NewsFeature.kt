package app.what.schedule.features.news

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.what.foundation.core.Feature
import app.what.navigation.core.NavComponent
import app.what.navigation.core.rememberNavigator
import app.what.schedule.features.news.domain.NewsController
import app.what.schedule.features.news.domain.models.NewsAction
import app.what.schedule.features.news.domain.models.NewsEvent
import app.what.schedule.features.news.navigation.NewsProvider
import app.what.schedule.features.news.presentation.NewsView
import app.what.schedule.features.newsDetail.navigation.NewsDetailProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NewsFeature(
    override val data: NewsProvider
) : Feature<NewsController, NewsEvent>(), NavComponent<NewsProvider>, KoinComponent {
    override val controller: NewsController by inject()

    @Composable
    override fun content(modifier: Modifier) = Column(
        modifier.fillMaxSize()
    ) {
        val viewState by controller.collectStates()
        val viewAction by controller.collectActions()
        val navigator = rememberNavigator()

        LaunchedEffect(Unit) {
            listener(NewsEvent.Init)
        }

        NewsView(viewState, listener)

        when (viewAction) {
            null -> Unit
            is NewsAction.NavigateToNewsDetail -> {
                (viewAction as NewsAction.NavigateToNewsDetail).item.let {
                    navigator.parent!!.c.navigate(
                        NewsDetailProvider(
                            it.id,
                            it.url,
                            it.bannerUrl,
                            it.title,
                            it.description
                        )
                    )
                }
                controller.clearAction()
            }
        }
    }
}
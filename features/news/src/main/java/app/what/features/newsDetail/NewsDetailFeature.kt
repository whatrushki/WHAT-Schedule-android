package app.what.schedule.features.newsDetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.what.foundation.core.Feature
import app.what.navigation.core.NavComponent
import app.what.domain.models.NewListItem
import app.what.schedule.features.newsDetail.domain.NewsDetailController
import app.what.schedule.features.newsDetail.domain.models.NewsDetailEvent
import app.what.schedule.features.newsDetail.navigation.NewsDetailProvider
import app.what.schedule.features.newsDetail.presentation.NewsDetailView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class NewsDetailFeature(
    override val data: NewsDetailProvider
) : Feature<NewsDetailController, NewsDetailEvent>(),
    NavComponent<NewsDetailProvider>,
    KoinComponent {
    override val controller: NewsDetailController by inject {
        parametersOf(
            NewListItem(
                data.id, data.url, data.bannerUrl, data.title, data.description,
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date, emptyList()
            )
        )
    }
    
    @Composable
    override fun content(modifier: Modifier) = Column(
        modifier.fillMaxSize()
    ) {
        val viewState by controller.collectStates()
        
        LaunchedEffect(Unit) {
            listener(NewsDetailEvent.Init)
        }
        
        NewsDetailView(viewState, listener)
    }
}
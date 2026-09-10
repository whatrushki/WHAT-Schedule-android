package app.what.schedule.features.news.domain.models

import app.what.domain.models.NewListItem


sealed interface NewsEvent {
    object Init : NewsEvent
    object OnListEndingScrolled : NewsEvent
    object OnRefresh : NewsEvent
    class OnNewEnterClicked(val value: NewListItem) : NewsEvent
}
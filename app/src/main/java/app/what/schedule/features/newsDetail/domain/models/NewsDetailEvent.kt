package app.what.schedule.features.newsDetail.domain.models


sealed interface NewsDetailEvent {
    object Init : NewsDetailEvent
    object OnRefresh : NewsDetailEvent
//    class OnSearchClicked(val value: NewsDetailSearch) : NewsDetailEvent
//    class OnSearchLongPressed(val value: NewsDetailSearch) : NewsDetailEvent
}
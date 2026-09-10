package app.what.schedule.features.schedule.domain.models

import app.what.domain.models.ScheduleSearch

sealed interface ScheduleEvent {
    object Init : ScheduleEvent
    object UpdateSchedule : ScheduleEvent
    object OnRefresh : ScheduleEvent
    object OnRefreshSearches : ScheduleEvent
    object OnCloudSync : ScheduleEvent
    class OnSearchClicked(val value: ScheduleSearch) : ScheduleEvent
    class OnSearchLongPressed(val value: ScheduleSearch) : ScheduleEvent
}
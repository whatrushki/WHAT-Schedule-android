package app.what.schedule.features.schedule.domain.models

import app.what.schedule.data.remote.api.ScheduleSearch

sealed interface ScheduleEvent {
    object Init : ScheduleEvent
    object UpdateSchedule : ScheduleEvent
    object OnRefresh : ScheduleEvent
    class OnSearchClicked(val value: ScheduleSearch) : ScheduleEvent
    class OnSearchLongPressed(val value: ScheduleSearch) : ScheduleEvent
}
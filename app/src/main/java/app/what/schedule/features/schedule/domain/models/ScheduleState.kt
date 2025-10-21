package app.what.schedule.features.schedule.domain.models

import app.what.foundation.data.RemoteState
import app.what.schedule.data.remote.api.DaySchedule
import app.what.schedule.data.remote.api.ScheduleSearch

data class ScheduleState(
    val schedules: List<DaySchedule> = emptyList(),
    val scheduleState: RemoteState = RemoteState.Nothing,
    val scheduleSearches: List<ScheduleSearch> = emptyList(),
    val scheduleSearchesState: RemoteState = RemoteState.Nothing,
    val search: ScheduleSearch? = null
)



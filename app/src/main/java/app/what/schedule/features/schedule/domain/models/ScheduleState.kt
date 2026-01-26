package app.what.schedule.features.schedule.domain.models

import app.what.foundation.data.RemoteState
import app.what.schedule.data.remote.api.models.DaySchedule
import app.what.schedule.data.remote.api.models.ScheduleSearch
import app.what.schedule.ui.components.ScheduleSearchData

data class ScheduleState(
    val schedules: List<DaySchedule> = emptyList(),
    val scheduleState: RemoteState = RemoteState.Idle,
    override val scheduleSearches: List<ScheduleSearch> = emptyList(),
    val scheduleSearchesState: RemoteState = RemoteState.Idle,
    override val selectedSearch: ScheduleSearch? = null
) : ScheduleSearchData



package app.what.schedule.features.schedule.domain.models

import app.what.foundation.data.RemoteState
import app.what.schedule.data.remote.api.DaySchedule
import app.what.schedule.data.remote.api.Group
import app.what.schedule.data.remote.api.ScheduleSearch
import app.what.schedule.data.remote.api.Teacher

data class ScheduleState(
    val schedules: List<DaySchedule> = emptyList(),
    val scheduleState: RemoteState = RemoteState.Nothing,
    val groups: List<Group> = emptyList(),
    val groupsState: RemoteState = RemoteState.Nothing,
    val teachers: List<Teacher> = emptyList(),
    val teachersState: RemoteState = RemoteState.Nothing,
    val search: ScheduleSearch? = null
)



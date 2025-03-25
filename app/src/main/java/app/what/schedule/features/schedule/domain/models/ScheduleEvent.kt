package app.what.schedule.features.schedule.domain.models

import app.what.schedule.data.remote.api.Group
import app.what.schedule.data.remote.api.ScheduleSearch
import app.what.schedule.data.remote.api.Teacher

sealed interface ScheduleEvent {
    object Init : ScheduleEvent
    object UpdateSchedule : ScheduleEvent
    object OnRefresh : ScheduleEvent
    class OnLessonItemTeacherClicked(val value: Teacher) : ScheduleEvent
    class OnLessonItemGroupClicked(val value: Group) : ScheduleEvent
    class OnSearchCompleted(val query: ScheduleSearch) : ScheduleEvent
}
package app.what.schedule.features.schedule.domain.models

import app.what.schedule.data.remote.api.ScheduleSearch

sealed interface ScheduleEvent {
    object Init : ScheduleEvent
    object UpdateSchedule : ScheduleEvent
    class OnLessonItemTeacherClicked(val value: String) : ScheduleEvent
    class OnLessonItemGroupClicked(val value: String) : ScheduleEvent
    class OnSearchCompleted(val query: ScheduleSearch) : ScheduleEvent
}
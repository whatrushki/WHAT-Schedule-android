package app.what.schedule.features.schedule.domain

import android.util.Log
import androidx.lifecycle.viewModelScope
import app.what.foundation.core.UIController
import app.what.foundation.data.RemoteState
import app.what.foundation.utils.orThrow
import app.what.foundation.utils.safeExecute
import app.what.foundation.utils.suspendCall
import app.what.schedule.data.local.settings.AppSettingsRepository
import app.what.schedule.data.remote.api.InstitutionManager
import app.what.schedule.data.remote.api.ScheduleSearch
import app.what.schedule.features.schedule.domain.models.ScheduleAction
import app.what.schedule.features.schedule.domain.models.ScheduleEvent
import app.what.schedule.features.schedule.domain.models.ScheduleState


class ScheduleController(
    private val institutionManager: InstitutionManager,
    private val settings: AppSettingsRepository
) : UIController<ScheduleState, ScheduleAction, ScheduleEvent>(
    ScheduleState()
) {
    private var api = institutionManager.getSavedProvider().orThrow { "No provider selected" }

    override fun obtainEvent(viewEvent: ScheduleEvent) = when (viewEvent) {
        ScheduleEvent.Init -> updateApiProvider()
        ScheduleEvent.UpdateSchedule -> updateSchedule(viewState.search)
        is ScheduleEvent.OnSearchCompleted -> updateSchedule(viewEvent.query)
        is ScheduleEvent.OnLessonItemGroupClicked -> updateSchedule(ScheduleSearch.Group(viewEvent.value))
        is ScheduleEvent.OnLessonItemTeacherClicked -> updateSchedule(
            ScheduleSearch.Teacher(viewEvent.value)
        )
    }

    init {
        val lastSearchedGroup = settings.getLastSearchedGroup()
        updateSchedule(if (lastSearchedGroup != null) ScheduleSearch.Group(lastSearchedGroup) else null)
        updateTeachers()
        updateGroups()
    }

    private fun updateApiProvider() {
        api = institutionManager.getSavedProvider().orThrow { "No provider selected" }
    }

    private fun updateSchedule(search: ScheduleSearch?) {
        updateState {
            copy(
                search = search,
                schedules = emptyList(),
                scheduleState = RemoteState.Loading
            )
        }

        suspendCall(viewModelScope) {
            Log.d("d", "Api: $api")

            val data = when (search) {
                is ScheduleSearch.Teacher -> {
                    settings.setLastSearchedTeacher(viewState.search!!.query)
                    api.getTeacherSchedule(viewState.search!!.query, true)
                }

                is ScheduleSearch.Group -> {
                    settings.setLastSearchedGroup(viewState.search!!.query)
                    api.getGroupSchedule(viewState.search!!.query, true)
                }

                null -> {
                    safeUpdateState { copy(scheduleState = RemoteState.Idle) }
                    return@suspendCall
                }


            }

            safeUpdateState {
                copy(
                    scheduleState = RemoteState.Success,
                    schedules = data
                )
            }
        }
    }

    private fun updateGroups() {
        safeExecute(
            scope = viewModelScope,
            failure = {
                safeUpdateState { copy(groupsState = RemoteState.Error) }
                Log.d("d", "Error: " + it.message)
            }
        ) {
            safeUpdateState { copy(groupsState = RemoteState.Loading) }

            val data = api.getGroups()

            safeUpdateState { copy(groups = data, groupsState = RemoteState.Success) }
        }
    }

    private fun updateTeachers() {
        safeExecute(
            scope = viewModelScope,
            failure = {
                safeUpdateState { copy(teachersState = RemoteState.Error) }
                Log.d("d", "Error: " + it.message)
            }
        ) {
            safeUpdateState { copy(teachersState = RemoteState.Error) }

            val data = api.getTeachers()

            safeUpdateState { copy(teachers = data, teachersState = RemoteState.Success) }
        }
    }
}
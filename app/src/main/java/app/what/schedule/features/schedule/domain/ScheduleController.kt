package app.what.schedule.features.schedule.domain

import android.util.Log
import androidx.lifecycle.viewModelScope
import app.what.foundation.core.UIController
import app.what.foundation.data.RemoteState
import app.what.foundation.utils.safeExecute
import app.what.foundation.utils.suspendCall
import app.what.schedule.data.local.settings.AppSettingsRepository
import app.what.schedule.data.remote.api.ScheduleSearch
import app.what.schedule.data.remote.api.toGroup
import app.what.schedule.data.remote.api.toScheduleSearch
import app.what.schedule.data.remote.api.toTeacher
import app.what.schedule.domain.ScheduleRepository
import app.what.schedule.features.schedule.domain.models.ScheduleAction
import app.what.schedule.features.schedule.domain.models.ScheduleEvent
import app.what.schedule.features.schedule.domain.models.ScheduleState
import kotlinx.coroutines.async


class ScheduleController(
    private val apiRepository: ScheduleRepository,
    private val settings: AppSettingsRepository
) : UIController<ScheduleState, ScheduleAction, ScheduleEvent>(
    ScheduleState()
) {
    override fun obtainEvent(viewEvent: ScheduleEvent) = when (viewEvent) {
        ScheduleEvent.Init -> init()
        ScheduleEvent.UpdateSchedule -> syncSchedule(viewState.search)
        ScheduleEvent.OnRefresh -> syncSchedule(viewState.search, false)
        is ScheduleEvent.OnSearchCompleted -> syncSchedule(viewEvent.query)
        is ScheduleEvent.OnLessonItemGroupClicked -> syncSchedule(viewEvent.value.toScheduleSearch())
        is ScheduleEvent.OnLessonItemTeacherClicked -> syncSchedule(viewEvent.value.toScheduleSearch())
    }

    private fun init() {
        if (viewState.scheduleState != RemoteState.Idle) return

        updateApiProvider()
        val lastSearchedGroup = settings.getLastSearchedGroup()
        updateState {
            copy(search = lastSearchedGroup?.let { ScheduleSearch.Group(it.name, it.id) })
        }

        safeExecute(scope = viewModelScope,
            failure = { Log.d("d", "Error: $it") }
        ) {
            val ut = async { updateTeachers() }
            val ug = async { updateGroups() }
            ut.await(); ug.await()

            updateSchedule(viewState.search, true)
        }
    }

    private fun syncGroups() {
        suspendCall(viewModelScope) { updateGroups() }
    }

    private fun syncTeachers() {
        suspendCall(viewModelScope) { updateTeachers() }
    }

    private fun syncSchedule(search: ScheduleSearch?, useCache: Boolean = true) {
        suspendCall(viewModelScope) { updateSchedule(search, useCache) }
    }

    private fun updateApiProvider() = apiRepository.updateApiProvider()

    private suspend fun updateSchedule(search: ScheduleSearch?, useCache: Boolean) {
        safeUpdateState {
            copy(search = search, schedules = emptyList(), scheduleState = RemoteState.Loading)
        }

        Log.d("d", "Api: $apiRepository")

        when (search) {
            is ScheduleSearch.Group -> settings.setLastSearchedGroup(search.toGroup())
            is ScheduleSearch.Teacher -> settings.setLastSearchedTeacher(search.toTeacher())
            else -> Unit
        }

        val data = apiRepository.getSchedule(search ?: return, useCache)

        safeUpdateState { copy(scheduleState = RemoteState.Success, schedules = data) }
    }

    private suspend fun updateGroups() {
        safeUpdateState { copy(groupsState = RemoteState.Loading) }
        val data = apiRepository.getGroups()
        safeUpdateState { copy(groups = data, groupsState = RemoteState.Success) }
    }

    private suspend fun updateTeachers() {
        safeUpdateState { copy(teachersState = RemoteState.Loading) }
        val data = apiRepository.getTeachers()
        safeUpdateState { copy(teachers = data, teachersState = RemoteState.Success) }
    }
}
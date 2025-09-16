package app.what.schedule.features.schedule.domain

import androidx.lifecycle.viewModelScope
import app.what.foundation.core.UIController
import app.what.foundation.data.RemoteState
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.safeExecute
import app.what.foundation.utils.suspendCall
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.remote.api.Group
import app.what.schedule.data.remote.api.ScheduleSearch
import app.what.schedule.data.remote.api.Teacher
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
    private val settings: AppValues
) : UIController<ScheduleState, ScheduleAction, ScheduleEvent>(
    ScheduleState()
) {
    override fun obtainEvent(viewEvent: ScheduleEvent) = when (viewEvent) {
        ScheduleEvent.Init -> {}
        ScheduleEvent.UpdateSchedule -> syncSchedule(viewState.search)
        ScheduleEvent.OnRefresh -> syncSchedule(viewState.search, false)
        is ScheduleEvent.OnSearchCompleted -> syncSchedule(viewEvent.query)
        is ScheduleEvent.OnLessonItemGroupClicked -> syncSchedule(viewEvent.value.toScheduleSearch())
        is ScheduleEvent.OnLessonItemTeacherClicked -> syncSchedule(viewEvent.value.toScheduleSearch())
        is ScheduleEvent.OnTeacherLongPressed -> toggleFavorites(viewEvent.value)
        is ScheduleEvent.OnGroupLongPressed -> toggleFavorites(viewEvent.value)
    }

    init {
        init()
    }


    private fun init() {
        val lastSearch = settings.lastSearch.get()

        updateState {
            if (lastSearch == null) copy(scheduleState = RemoteState.Idle)
            else copy(search = lastSearch)
        }

        updateApiProvider()

        safeExecute(
            scope = viewModelScope,
            failure = { Auditor.debug("d", "Error: $it") }
        ) {
            val ut = async { updateTeachers() }
            val ug = async { updateGroups() }
            ut.await(); ug.await()

            updateSchedule(viewState.search, true)
        }
    }

    private fun toggleFavorites(value: Group) {
        suspendCall(viewModelScope) {
            apiRepository.toggleFavorites(value)
        }.invokeOnCompletion {
            syncGroups()
            Auditor.debug("d", "synced")
        }
    }

    private fun toggleFavorites(value: Teacher) {
        suspendCall(viewModelScope) {
            apiRepository.toggleFavorites(value)
        }.invokeOnCompletion {
            syncTeachers()
            Auditor.debug("d", "synced")
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
        search ?: return

        safeUpdateState {
            copy(search = search, schedules = emptyList(), scheduleState = RemoteState.Loading)
        }

        Auditor.debug("d", "Api: $apiRepository")

        settings.lastSearch.set(search)

        val data = apiRepository.getSchedule(search, useCache)

        safeUpdateState { copy(scheduleState = RemoteState.Success, schedules = data) }
    }

    private suspend fun updateGroups() {
        safeUpdateState { copy(groupsState = RemoteState.Loading) }
        val data = apiRepository.getGroups()
        Auditor.debug(
            "d",
            "groups" + data.filter { it.favorite }.joinToString { "${it.name} | ${it.favorite}" })
        safeUpdateState { copy(groups = data, groupsState = RemoteState.Success) }
    }

    private suspend fun updateTeachers() {
        safeUpdateState { copy(teachersState = RemoteState.Loading) }
        val data = apiRepository.getTeachers()
        Auditor.debug(
            "d",
            "teachers" + data.filter { it.favorite }.joinToString { "${it.name} | ${it.favorite}" })
        safeUpdateState { copy(teachers = data, teachersState = RemoteState.Success) }
    }
}
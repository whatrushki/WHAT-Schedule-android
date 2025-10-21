package app.what.schedule.features.schedule.domain

import androidx.lifecycle.viewModelScope
import app.what.foundation.core.UIController
import app.what.foundation.data.RemoteState
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.safeExecute
import app.what.foundation.utils.suspendCall
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.remote.api.ScheduleSearch
import app.what.schedule.data.remote.api.toScheduleSearch
import app.what.schedule.domain.ScheduleRepository
import app.what.schedule.features.schedule.domain.models.ScheduleAction
import app.what.schedule.features.schedule.domain.models.ScheduleEvent
import app.what.schedule.features.schedule.domain.models.ScheduleState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll


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
        is ScheduleEvent.OnSearchClicked -> syncSchedule(viewEvent.value)
        is ScheduleEvent.OnSearchLongPressed -> toggleFavorites(viewEvent.value)
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

        updateSearches()
        syncSchedule(viewState.search, true)
    }

    private fun toggleFavorites(value: ScheduleSearch) {
        suspendCall(viewModelScope) {
            when (value) {
                is ScheduleSearch.Group -> apiRepository.toggleFavorites(value)
                is ScheduleSearch.Teacher -> apiRepository.toggleFavorites(value)
            }
        }.invokeOnCompletion {
            updateSearches()
            Auditor.debug("d", "synced")
        }
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

    private fun updateSearches() {
        safeExecute(
            scope = viewModelScope,
            failure = {
                safeUpdateState {
                    copy(scheduleSearchesState = RemoteState.Error(it))
                }
            }
        ) {
            safeUpdateState { copy(scheduleSearchesState = RemoteState.Loading) }

            val ut = async { apiRepository.getTeachers().map { it.toScheduleSearch() } }
            val ug = async { apiRepository.getGroups().map { it.toScheduleSearch() } }
            val data = awaitAll(ut, ug).flatten()

            safeUpdateState {
                copy(
                    scheduleSearches = data,
                    scheduleSearchesState = RemoteState.Success
                )
            }
        }
    }
}
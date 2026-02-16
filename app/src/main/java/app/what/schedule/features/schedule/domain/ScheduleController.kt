package app.what.schedule.features.schedule.domain

import androidx.lifecycle.viewModelScope
import app.what.foundation.core.UIController
import app.what.foundation.data.RemoteState
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.launchIO
import app.what.foundation.utils.launchSafe
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.remote.api.ScheduleResponse
import app.what.schedule.data.remote.api.models.ScheduleSearch
import app.what.schedule.data.remote.api.models.toScheduleSearch
import app.what.schedule.domain.ScheduleRepository
import app.what.schedule.features.schedule.domain.models.ScheduleAction
import app.what.schedule.features.schedule.domain.models.ScheduleEvent
import app.what.schedule.features.schedule.domain.models.ScheduleState
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll


class ScheduleController(
    private val apiRepository: ScheduleRepository,
    private val settings: AppValues
) : UIController<ScheduleState, ScheduleAction, ScheduleEvent>(
    ScheduleState()
) {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    override fun obtainEvent(viewEvent: ScheduleEvent) = when (viewEvent) {
        ScheduleEvent.Init -> {}
        ScheduleEvent.UpdateSchedule -> syncSchedule(viewState.selectedSearch)
        ScheduleEvent.OnRefresh -> syncSchedule(viewState.selectedSearch, false)
        is ScheduleEvent.OnSearchClicked -> syncSchedule(viewEvent.value)
        is ScheduleEvent.OnSearchLongPressed -> toggleFavorites(viewEvent.value)
    }

    init {
        init()
    }

    val debugMode: Boolean
        get() = settings.debugMode.get() == true

    private fun init() {
        val lastSearch = settings.lastSearch.get()

        updateState {
            if (lastSearch == null) copy(scheduleState = RemoteState.Idle)
            else copy(selectedSearch = lastSearch)
        }

        updateSearches()
        syncSchedule(viewState.selectedSearch, true)
    }

    private fun toggleFavorites(value: ScheduleSearch) {
        viewModelScope.launchIO {
            when (value) {
                is ScheduleSearch.Group -> apiRepository.toggleFavorites(value)
                is ScheduleSearch.Teacher -> apiRepository.toggleFavorites(value)
            }
        }.invokeOnCompletion {
            updateSearches()
            val scheduleTag = buildTag(LogScope.SCHEDULE, LogCat.STATE)
            Auditor.debug(scheduleTag, "Избранное обновлено")
        }
    }

    private fun syncSchedule(search: ScheduleSearch?, useCache: Boolean = true) {
        val scheduleTag = buildTag(LogScope.SCHEDULE, LogCat.STATE)
        Auditor.debug(scheduleTag, "Синхронизация расписания: $search, кеш: $useCache")

        viewModelScope.launchSafe(
            debug = debugMode,
            onFailure = {
                Auditor.err(scheduleTag, "Ошибка синхронизации расписания", it)
                crashlytics.setCustomKey("schedule_sync_error", it.message ?: "unknown")
                crashlytics.recordException(it)
                updateState { copy(scheduleState = RemoteState.Error(it)) }
            }
        ) {
            val searchId = apiRepository.findSearchId(search)
            if (search != null && searchId != null)
                updateSchedule(search.copy(id = searchId), useCache)
        }
    }

    private suspend fun updateSchedule(search: ScheduleSearch?, useCache: Boolean) {
        search ?: return

        updateState {
            copy(
                selectedSearch = search,
                schedules = emptyList(),
                scheduleState = RemoteState.Loading
            )
        }

        val scheduleTag = buildTag(LogScope.SCHEDULE, LogCat.STATE)
        settings.lastSearch.set(search)
        val data = apiRepository.getSchedule(search, useCache, viewState.schedules.isEmpty())

        when (data) {
            is ScheduleResponse.Available -> {
                Auditor.debug(
                    scheduleTag,
                    "Расписание успешно получено, дней: ${data.schedules.size}"
                )
            }

            ScheduleResponse.Empty -> {
                Auditor.debug(scheduleTag, "Расписание пустое")
            }

            ScheduleResponse.UpToDate -> {
                Auditor.debug(scheduleTag, "Расписание актуально")
            }
        }

        updateState {
            when (data) {
                is ScheduleResponse.Available -> copy(
                    scheduleState = RemoteState.Success,
                    schedules = data.schedules
                )

                ScheduleResponse.Empty -> copy(
                    scheduleState = RemoteState.Empty,
                    schedules = emptyList()
                )

                ScheduleResponse.UpToDate -> copy(scheduleState = RemoteState.Success)
            }
        }
    }

    private fun updateSearches() {
        viewModelScope.launchSafe(
            onFailure = {
                updateState {
                    copy(scheduleSearchesState = RemoteState.Error(it))
                }
            }
        ) {
            updateState { copy(scheduleSearchesState = RemoteState.Loading) }

            val ut = async { apiRepository.getTeachers().map { it.toScheduleSearch() } }
            val ug = async { apiRepository.getGroups().map { it.toScheduleSearch() } }
            val data = awaitAll(ut, ug).flatten()

            updateState {
                copy(
                    scheduleSearches = data,
                    scheduleSearchesState = RemoteState.Success
                )
            }
        }
    }
}
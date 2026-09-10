package app.what.schedule.features.schedule.domain

import androidx.lifecycle.viewModelScope
import app.what.foundation.core.UIController
import app.what.foundation.data.RemoteState
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.launchIO
import app.what.foundation.utils.launchSafe
import app.what.schedule.data.local.settings.AppValues
import app.what.domain.models.ScheduleResponse
import app.what.domain.models.ScheduleSearch
import app.what.domain.models.toScheduleSearch
import app.what.domain.repositories.ScheduleRepository
import app.what.schedule.features.schedule.domain.models.ScheduleAction
import app.what.schedule.features.schedule.domain.models.ScheduleEvent
import app.what.schedule.features.schedule.domain.models.ScheduleState
import app.what.foundation.utils.LogCat
import app.what.foundation.utils.LogScope
import app.what.foundation.utils.buildTag
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


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
        ScheduleEvent.OnCloudSync -> syncSchedule(
            viewState.selectedSearch,
            useCache = false,
            cloudSync = true
        )
        
        ScheduleEvent.OnRefresh -> syncSchedule(viewState.selectedSearch, false)
        ScheduleEvent.OnRefreshSearches -> updateSearches()
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
    
    private fun syncSchedule(
        search: ScheduleSearch?,
        useCache: Boolean = true,
        cloudSync: Boolean = false
    ) {
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
            val searchId = apiRepository.findSearchId(search) ?: search?.id?.takeIf { it.isNotEmpty() }
            if (search != null && searchId != null)
                updateSchedule(search.copy(id = searchId), useCache, cloudSync)
        }
    }
    
    private suspend fun updateSchedule(
        search: ScheduleSearch?,
        useCache: Boolean,
        cloudSync: Boolean = false
    ) {
        search ?: return
        val scheduleTag = buildTag(LogScope.SCHEDULE, LogCat.STATE)
        val groupChanged = settings.lastSearch.get() != search
        
        updateState {
            copy(selectedSearch = search, schedules = if (groupChanged) emptyList() else viewState.schedules, scheduleState = RemoteState.Loading)
        }
        
        settings.lastSearch.set(search)
        val data = apiRepository.getSchedule(
            search,
            useCache && !groupChanged,
            viewState.schedules.isEmpty() || groupChanged,
            cloudSync
        )
        
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
            
            else -> {
                Auditor.debug(scheduleTag, "Не удалось получить расписание")
            }
        }
        
        updateState {
            when (data) {
                ScheduleResponse.UpToDate -> copy(scheduleState = RemoteState.Success)
                is ScheduleResponse.Available -> copy(
                    scheduleState = RemoteState.Success,
                    schedules = data.schedules
                )
                
                is ScheduleResponse.Error -> copy(
                    scheduleState = RemoteState.Error(data.exception),
                    schedules = data.cachedSchedules ?: emptyList()
                )
                
                ScheduleResponse.Empty -> copy(
                    scheduleState = RemoteState.Empty,
                    schedules = emptyList()
                )
            }
        }
    }
    
    private fun updateSearches() {
        viewModelScope.launchSafe(
            retryCount = 0,
            onFailure = {
                val scheduleTag = buildTag(LogScope.SCHEDULE, LogCat.STATE)
                Auditor.err(scheduleTag, "Ошибка загрузки групп/преподавателей", it)
                updateState {
                    copy(scheduleSearchesState = RemoteState.Error(it))
                }
            }
        ) {
            updateState { copy(scheduleSearchesState = RemoteState.Loading) }
            
            val (teachers, groups) = coroutineScope {
                val ut = async { apiRepository.getTeachers().map { it.toScheduleSearch() } }
                val ug = async { apiRepository.getGroups().map { it.toScheduleSearch() } }
                ut.await() to ug.await()
            }
            val data = (teachers + groups).distinctBy { "${it::class.simpleName}_${it.name.trim()}" }
            if (data.isEmpty()) {
                throw java.io.IOException("Не удалось загрузить список групп")
            }
            
            updateState {
                copy(
                    scheduleSearches = data,
                    scheduleSearchesState = RemoteState.Success
                )
            }
        }
    }
}
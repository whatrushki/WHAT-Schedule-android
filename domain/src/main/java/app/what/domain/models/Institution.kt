package app.what.domain.models

import java.time.LocalDateTime

data class MetaInfo(
    val id: String,
    val name: String,
    val fullName: String,
    val description: String,
    val sourceTypes: Set<SourceType>,
    val sourceUrl: String
) {
    companion object {
        fun empty() = MetaInfo(
            id = "",
            name = "",
            fullName = "",
            description = "",
            sourceTypes = emptySet(),
            sourceUrl = ""
        )
    }
}

enum class SourceType { API, PARSER, EXCEL, PDF }

sealed interface ScheduleResponse {
    object Empty : ScheduleResponse
    object UpToDate : ScheduleResponse
    class Error(
        val cachedSchedules: List<DaySchedule>?,
        val lastModified: LocalDateTime?,
        val exception: Exception
    ) : ScheduleResponse
    
    sealed class Available(
        val schedules: List<DaySchedule>,
        val lastModified: LocalDateTime
    ) : ScheduleResponse {
        class FromSource(
            schedules: List<DaySchedule>,
            lastModified: LocalDateTime
        ) : Available(schedules, lastModified)
        
        class FromCache(
            schedules: List<DaySchedule>,
            lastModified: LocalDateTime
        ) : Available(schedules, lastModified)
    }
}

fun List<ScheduleResponse>.sum(): ScheduleResponse {
    if (isEmpty()) return ScheduleResponse.Empty
    
    val allSchedules = mutableListOf<DaySchedule>()
    var latestModified: LocalDateTime? = null
    
    for (response in this) {
        when (response) {
            ScheduleResponse.Empty, ScheduleResponse.UpToDate -> {}
            is ScheduleResponse.Available -> {
                allSchedules.addAll(response.schedules)
                if (latestModified == null || response.lastModified > latestModified) {
                    latestModified = response.lastModified
                }
            }
            is ScheduleResponse.Error -> {}
        }
    }
    
    if (allSchedules.isEmpty()) {
        return ScheduleResponse.Empty
    }
    
    val hasFromSource = this.any { it is ScheduleResponse.Available.FromSource }
    val hasFromCache = this.any { it is ScheduleResponse.Available.FromCache }
    val lastModified = latestModified ?: LocalDateTime.now()
    
    return when {
        hasFromSource -> ScheduleResponse.Available.FromSource(allSchedules, lastModified)
        hasFromCache -> ScheduleResponse.Available.FromCache(allSchedules, lastModified)
        else -> ScheduleResponse.Available.FromSource(allSchedules, lastModified)
    }
}

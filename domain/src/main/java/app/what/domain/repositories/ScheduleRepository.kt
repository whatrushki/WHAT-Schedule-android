package app.what.domain.repositories

import app.what.domain.models.Group
import app.what.domain.models.ScheduleResponse
import app.what.domain.models.ScheduleSearch
import app.what.domain.models.Teacher

interface ScheduleRepository {
    suspend fun getGroups(): List<Group>
    suspend fun getTeachers(): List<Teacher>
    suspend fun toggleFavorites(value: ScheduleSearch.Group)
    suspend fun toggleFavorites(value: ScheduleSearch.Teacher)
    suspend fun findSearchId(search: ScheduleSearch?): String?
    suspend fun getSchedule(
        search: ScheduleSearch,
        useCache: Boolean,
        requiresData: Boolean = true,
        cloudSync: Boolean = false
    ): ScheduleResponse
}

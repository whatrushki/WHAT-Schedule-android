package app.what.data.adapters

import app.what.data.mappers.toDomain
import app.what.data.mappers.toDomainResponse
import app.what.domain.models.Group
import app.what.domain.models.NewItem
import app.what.domain.models.NewListItem
import app.what.domain.models.ScheduleResponse
import app.what.domain.models.Teacher
import app.what.schedule.core.clients.NewsClient
import app.what.schedule.core.clients.ScheduleClient
import app.what.schedule.data.remote.api.AdditionalData
import app.what.schedule.data.remote.api.NewsService
import app.what.schedule.data.remote.api.ScheduleService

class AdaptedScheduleService(
    private val client: ScheduleClient
) : ScheduleService {

    override suspend fun getGroupSchedule(
        group: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): ScheduleResponse = try {
        client.getGroupSchedule(group, showReplacements).toDomainResponse()
    } catch (e: Exception) {
        ScheduleResponse.Error(null, null, e)
    }

    override suspend fun getTeacherSchedule(
        teacher: String,
        showReplacements: Boolean,
        additional: AdditionalData
    ): ScheduleResponse = try {
        client.getTeacherSchedule(teacher, showReplacements).toDomainResponse()
    } catch (e: Exception) {
        ScheduleResponse.Error(null, null, e)
    }

    override suspend fun getGroups(): List<Group> = try {
        client.getGroups().map { it.toDomain() }
    } catch (_: Exception) {
        emptyList()
    }

    override suspend fun getTeachers(): List<Teacher> = try {
        client.getTeachers().map { it.toDomain() }
    } catch (_: Exception) {
        emptyList()
    }
}

class AdaptedNewsService(
    private val client: NewsClient?
) : NewsService {

    override suspend fun getNews(page: Int): List<NewListItem> = try {
        client?.getNews(page)?.map { it.toDomain() } ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }

    override suspend fun getNewDetail(id: String): NewItem =
        client?.getNewDetail(id)?.toDomain() ?: error("News not found: $id")
}

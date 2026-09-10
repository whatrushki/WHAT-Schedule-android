package app.what.schedule.core.clients

import app.what.schedule.core.models.DayScheduleDto
import app.what.schedule.core.models.GroupDto
import app.what.schedule.core.models.InstitutionMetaDto
import app.what.schedule.core.models.NewDetailDto
import app.what.schedule.core.models.NewListItemDto
import app.what.schedule.core.models.TeacherDto

interface ScheduleClient {
    suspend fun getGroupSchedule(group: String, showReplacements: Boolean = true): List<DayScheduleDto>
    suspend fun getTeacherSchedule(teacher: String, showReplacements: Boolean = true): List<DayScheduleDto>
    suspend fun getGroups(): List<GroupDto>
    suspend fun getTeachers(): List<TeacherDto>
}

interface NewsClient {
    suspend fun getNews(page: Int): List<NewListItemDto>
    suspend fun getNewDetail(id: String): NewDetailDto
}

interface InstitutionProvider {
    val metadata: InstitutionMetaDto
    val scheduleClient: ScheduleClient
    val newsClient: NewsClient?
}

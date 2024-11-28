package app.what.schedule.data.remote.api

import io.ktor.client.HttpClient

abstract class ScheduleApi(
    private val client: HttpClient
) {
    abstract suspend fun getGroupSchedule(group: String): List<DaySchedule>
    abstract suspend fun getTeacherSchedule(teacher: String): List<DaySchedule>
    abstract suspend fun getGroups(): List<String>
    abstract suspend fun getTeachers(): List<String>

    fun getReplacements(): List<Replacement> = emptyList()
}

class Replacement
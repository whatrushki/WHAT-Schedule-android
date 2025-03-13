package app.what.schedule.data.remote.api

interface ScheduleApi {
    val lessonsSchedule: LessonsSchedule
    suspend fun getGroupSchedule(group: String, showReplacements: Boolean = false): List<DaySchedule>
    suspend fun getTeacherSchedule(teacher: String, showReplacements: Boolean = false): List<DaySchedule>
    suspend fun getGroups(): List<Group>
    suspend fun getTeachers(): List<Teacher>
}
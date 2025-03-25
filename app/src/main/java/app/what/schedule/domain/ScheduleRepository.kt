package app.what.schedule.domain

import android.util.Log
import androidx.room.Transaction
import androidx.room.withTransaction
import app.what.foundation.utils.orThrow
import app.what.foundation.utils.suspendCall
import app.what.schedule.data.local.database.AppDatabase
import app.what.schedule.data.local.database.DayScheduleDBO
import app.what.schedule.data.local.database.DayScheduleSDBO
import app.what.schedule.data.local.database.GroupDBO
import app.what.schedule.data.local.database.LessonDBO
import app.what.schedule.data.local.database.OneTimeUnitDBO
import app.what.schedule.data.local.database.RequestDBO
import app.what.schedule.data.local.database.RequestSDBO
import app.what.schedule.data.local.database.TeacherDBO
import app.what.schedule.data.remote.api.DaySchedule
import app.what.schedule.data.remote.api.Group
import app.what.schedule.data.remote.api.InstitutionManager
import app.what.schedule.data.remote.api.Lesson
import app.what.schedule.data.remote.api.OneTimeUnit
import app.what.schedule.data.remote.api.ScheduleSearch
import app.what.schedule.data.remote.api.Teacher
import java.time.LocalDate

class ScheduleRepository(
    private val db: AppDatabase,
    private val institutionManager: InstitutionManager
) {
    private var api = institutionManager.getSavedProvider().orThrow { "No provider selected" }
    private fun getFilialId() = institutionManager.getSavedFilial()!!.metadata.id

    fun updateApiProvider() {
        api = institutionManager.getSavedProvider().orThrow { "No provider selected" }
    }

    suspend fun getGroups(): List<Group> = db.groupsDao
        .selectByInstitution(getFilialId())
        .map { Group(it.name, it.groupId) }
        .also { Log.d("d", if (it.isEmpty()) "group from cache" else "group from db") }
        .ifEmpty {
            api.getGroups()
                .also { db.withTransaction { it.forEach { saveGroup(getFilialId(), it) } } }
        }

    suspend fun getTeachers(): List<Teacher> = db.teachersDao
        .selectByInstitution(getFilialId())
        .map { Teacher(it.name, it.teacherId) }
        .also { Log.d("d", if (it.isEmpty()) "teacher from cache" else "teacher from db") }
        .ifEmpty {
            api.getTeachers()
                .also { db.withTransaction { it.forEach { saveTeacher(getFilialId(), it) } } }
        }

    suspend fun getSchedule(search: ScheduleSearch, useCache: Boolean): List<DaySchedule> {
        Log.d("d", "getSchedule repo")

        val request: RequestSDBO? = if (!useCache) null
        else db.requestsDao.selectLast(getFilialId(), search.id)

        Log.d("d", "request: $request")

        Log.d("d", "search: $search")

        Log.d(
            "d",
            "request != null && request.request.createdAt < LocalDate.now().minusDays(1): ${
                request != null && request.request.createdAt < LocalDate.now().minusDays(1)
            }"
        )

        return if (
            request != null &&
            request.request.createdAt == LocalDate.now()
        ) {
            Log.d("d", "from cache")
            request.daySchedules.map { it.toDaySchedule() }
        } else {
            Log.d("d", "from api")
            val daySchedules = when (search) {
                is ScheduleSearch.Group -> api.getGroupSchedule(search.id, true)
                is ScheduleSearch.Teacher -> api.getTeacherSchedule(search.id, true)
            }

            suspendCall {
                saveRequest(getFilialId(), search.id, daySchedules)
            }

            daySchedules
        }
    }

    private suspend fun saveGroup(
        institutionId: String,
        it: Group
    ) = db.groupsDao.insert(
        GroupDBO(
            institutionId = institutionId,
            name = it.name,
            groupId = it.id,
        )
    )

    private suspend fun saveTeacher(
        institutionId: String,
        it: Teacher
    ) = db.teachersDao.insert(
        TeacherDBO(
            institutionId = institutionId,
            name = it.name,
            teacherId = it.id,
        )
    )

    @Transaction
    private suspend fun saveRequest(
        institutionId: String,
        query: String,
        daySchedules: List<DaySchedule>
    ) {
        // Вставляем запрос
        val requestId = db.requestsDao.insert(
            RequestDBO(
                institutionId = institutionId,
                query = query
            )
        )

        // Для каждого расписания дня
        daySchedules.forEach { daySchedule ->
            // Вставляем расписание дня
            val dayScheduleId = db.daySchedulesDao.insert(
                DayScheduleDBO(
                    fromRequest = requestId,
                    date = daySchedule.date,
                    scheduleType = daySchedule.scheduleType
                )
            )

            // Для каждого урока в расписании дня
            daySchedule.lessons.forEach { lesson ->
                // Вставляем урок
                val lessonId = db.lessonsDao.insert(
                    LessonDBO(
                        fromDay = dayScheduleId,
                        number = lesson.number,
                        startTime = lesson.startTime,
                        endTime = lesson.endTime,
                        subject = lesson.subject,
                        type = lesson.type,
                        state = lesson.state
                    )
                )

                // Для каждой единицы времени в уроке
                lesson.otUnits.forEach { otUnit ->
                    // Вставляем группу, если она еще не существует
                    val groupId = db.groupsDao
                        .selectIdByGroupId(institutionId, otUnit.group.id)
                        ?: db.groupsDao.insert(
                            GroupDBO(
                                institutionId = institutionId,
                                name = otUnit.group.name,
                                groupId = otUnit.group.id,
                                year = otUnit.group.year
                            )
                        )

                    // Вставляем учителя, если он еще не существует
                    val teacherId = db.teachersDao
                        .selectIdByTeacherId(institutionId, otUnit.teacher.id)
                        ?: db.teachersDao.insert(
                            TeacherDBO(
                                institutionId = institutionId,
                                name = otUnit.teacher.name,
                                teacherId = otUnit.teacher.id
                            )
                        )

                    // Вставляем единицу времени
                    db.otUnitsDao.insert(
                        OneTimeUnitDBO(
                            lessonId = lessonId,
                            groupId = groupId,
                            teacherId = teacherId,
                            auditory = otUnit.auditory,
                            building = otUnit.building
                        )
                    )
                }
            }
        }
    }
}


private fun DayScheduleSDBO.toDaySchedule() = DaySchedule(
    daySchedule.date,
    "",
    daySchedule.scheduleType,
    lessons.map {
        Lesson(
            number = it.lesson.number,
            subject = it.lesson.subject,
            type = it.lesson.type,
            startTime = it.lesson.startTime,
            endTime = it.lesson.endTime,
            state = it.lesson.state,
            otUnits = it.otUnits.map {
                OneTimeUnit(
                    group = Group(
                        it.group.name,
                        it.group.groupId
                    ),
                    teacher = Teacher(
                        it.teacher.name,
                        it.teacher.teacherId
                    ),
                    building = it.unit.building,
                    auditory = it.unit.auditory
                )
            }
        )
    }
)

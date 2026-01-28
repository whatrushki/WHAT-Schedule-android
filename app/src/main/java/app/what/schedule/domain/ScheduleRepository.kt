package app.what.schedule.domain

import androidx.room.withTransaction
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.launchIO
import app.what.foundation.utils.orThrow
import app.what.schedule.data.local.database.AppDatabase
import app.what.schedule.data.local.database.DayScheduleDBO
import app.what.schedule.data.local.database.GroupDBO
import app.what.schedule.data.local.database.LessonDBO
import app.what.schedule.data.local.database.OneTimeUnitDBO
import app.what.schedule.data.local.database.RequestDBO
import app.what.schedule.data.local.database.RequestSDBO
import app.what.schedule.data.local.database.TeacherDBO
import app.what.schedule.data.remote.api.AdditionalData
import app.what.schedule.data.remote.api.InstitutionManager
import app.what.schedule.data.remote.api.ScheduleResponse
import app.what.schedule.data.remote.api.models.DaySchedule
import app.what.schedule.data.remote.api.models.Group
import app.what.schedule.data.remote.api.models.ScheduleSearch
import app.what.schedule.data.remote.api.models.Teacher
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDate
import java.time.LocalDateTime

class ScheduleRepository(
    private val db: AppDatabase,
    private val institutionManager: InstitutionManager,
    private val scope: CoroutineScope
) {
    private val api
        get() = institutionManager.getSavedInstitution().orThrow { "No provider selected" }

    private fun getFilialId() = api.metadata.id

    suspend fun toggleFavorites(value: ScheduleSearch.Group) {
        db.withTransaction {
            val group = db.groupsDao.selectByGroupId(getFilialId(), value.id)
            db.groupsDao.update(group.copy(favorite = !group.favorite))
        }
    }

    suspend fun toggleFavorites(value: ScheduleSearch.Teacher) {
        db.withTransaction {
            val teacher = db.teachersDao.selectByTeacherId(getFilialId(), value.id)
            db.teachersDao.update(teacher.copy(favorite = !teacher.favorite))
        }
    }

    suspend fun getGroups(): List<Group> = db.groupsDao
        .selectByInstitution(getFilialId())
        .map { it.toModel() }
        .also { Auditor.debug("d", if (it.isEmpty()) "group from cache" else "group from db") }
        .ifEmpty {
            api.getGroups()
                .also { db.withTransaction { it.forEach { saveGroup(getFilialId(), it) } } }
        }

    suspend fun getTeachers(): List<Teacher> = db.teachersDao
        .selectByInstitution(getFilialId())
        .map { it.toModel() }
        .also { Auditor.debug("d", if (it.isEmpty()) "teacher from cache" else "teacher from db") }
        .ifEmpty {
            api.getTeachers()
                .also { db.withTransaction { it.forEach { saveTeacher(getFilialId(), it) } } }
        }

    suspend fun getSchedule(
        search: ScheduleSearch,
        useCache: Boolean,
        requiresData: Boolean = true
    ): ScheduleResponse {
        Auditor.debug("d", "getSchedule repo")

//        Auditor.debug("d", "request: $request")
//        Auditor.debug("d", "search: $search")
//        Auditor.debug(
//            "d",
//            "request != null && request.request.createdAt < LocalDate.now().minusDays(1): ${
//                request != null && request.createdAt < LocalDate.now().minusDays(1)
//            }"
//        )

        val lastRequest = db.requestsDao.selectLastOfInstitution(getFilialId())
        val cache: RequestSDBO? = db.requestsDao.selectLastWithData(getFilialId(), search.id)
        Auditor.debug("d", "cache=${cache}")

        return if (
            cache != null
            && useCache
            && cache.request.createdAt == LocalDate.now()
        ) {
            Auditor.debug("d", "from cache")
            ScheduleResponse.Available.FromCache(
                cache.daySchedules.map { it.toModel() },
                cache.request.lastModified
            )
        } else {
            Auditor.debug("d", "from api | lastModified=${cache?.request?.lastModified}")
            Auditor.debug("d", "from api | cache=${cache}")
            Auditor.debug("d", "from api | cache != null==${cache == null}")
            Auditor.debug("d", "from api | requiresData==${requiresData}")
            Auditor.debug("d", "from api | requiresData=${(cache == null || requiresData)}")

            val fetchSchedule: suspend (String, Boolean, AdditionalData) -> ScheduleResponse =
                if (search is ScheduleSearch.Group) api::getGroupSchedule
                else api::getTeacherSchedule

            val response = fetchSchedule(
                search.id, true, mapOf(
                    "lastModified" to lastRequest?.lastModified,
                    "requiresData" to (cache == null || requiresData)
                )
            )

            if (response is ScheduleResponse.Available) scope.launchIO {
                db.withTransaction {
                    db.requestsDao.deleteAll(getFilialId(), search.id)
                    saveRequest(
                        getFilialId(),
                        search.id,
                        response.lastModified,
                        response.schedules
                    )
                }
            }

            response
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

    private suspend fun saveRequest(
        institutionId: String,
        query: String,
        lastModified: LocalDateTime,
        daySchedules: List<DaySchedule>
    ) {
        db.withTransaction {
            // Вставляем запрос
            val requestId = db.requestsDao.insert(
                RequestDBO(
                    institutionId = institutionId,
                    query = query,
                    lastModified = lastModified
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
}


package app.what.schedule.domain

import androidx.room.withTransaction
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
import app.what.schedule.utils.Analytics
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
        val dbTag = buildTag(LogScope.DATABASE, LogCat.DB)
        Auditor.debug(dbTag, "Переключение избранного для группы: ${value.id}")
        
        db.withTransaction {
            val group = db.groupsDao.selectByGroupId(getFilialId(), value.id)
            val newFavoriteState = !group.favorite
            db.groupsDao.update(group.copy(favorite = newFavoriteState))
            Auditor.debug(dbTag, "Группа ${value.id} теперь ${if (newFavoriteState) "в избранном" else "не в избранном"}")
        }
    }

    suspend fun toggleFavorites(value: ScheduleSearch.Teacher) {
        val dbTag = buildTag(LogScope.DATABASE, LogCat.DB)
        Auditor.debug(dbTag, "Переключение избранного для преподавателя: ${value.id}")
        
        db.withTransaction {
            val teacher = db.teachersDao.selectByTeacherId(getFilialId(), value.id)
            val newFavoriteState = !teacher.favorite
            db.teachersDao.update(teacher.copy(favorite = newFavoriteState))
            Auditor.debug(dbTag, "Преподаватель ${value.id} теперь ${if (newFavoriteState) "в избранном" else "не в избранном"}")
        }
    }

    suspend fun getGroups(): List<Group> {
        val dbTag = buildTag(LogScope.DATABASE, LogCat.DB)
        val groups = db.groupsDao
            .selectByInstitution(getFilialId())
            .map { it.toModel() }
        
        return if (groups.isEmpty()) {
            Auditor.debug(dbTag, "Группы не найдены в БД, загрузка из API")
            api.getGroups().also { 
                db.withTransaction { it.forEach { saveGroup(getFilialId(), it) } }
                Auditor.debug(dbTag, "Загружено групп из API: ${it.size}")
            }
        } else {
            Auditor.debug(dbTag, "Группы загружены из БД: ${groups.size}")
            groups
        }
    }

    suspend fun getTeachers(): List<Teacher> {
        val dbTag = buildTag(LogScope.DATABASE, LogCat.DB)
        val teachers = db.teachersDao
            .selectByInstitution(getFilialId())
            .map { it.toModel() }
        
        return if (teachers.isEmpty()) {
            Auditor.debug(dbTag, "Преподаватели не найдены в БД, загрузка из API")
            api.getTeachers().also { 
                db.withTransaction { it.forEach { saveTeacher(getFilialId(), it) } }
                Auditor.debug(dbTag, "Загружено преподавателей из API: ${it.size}")
            }
        } else {
            Auditor.debug(dbTag, "Преподаватели загружены из БД: ${teachers.size}")
            teachers
        }
    }

    suspend fun getSchedule(
        search: ScheduleSearch,
        useCache: Boolean,
        requiresData: Boolean = true
    ): ScheduleResponse {
        Analytics.logScheduleRequest(search.name, search::class.simpleName.toString())
        val scheduleTag = buildTag(LogScope.SCHEDULE, LogCat.DB)
        val searchType = if (search is ScheduleSearch.Group) "группа" else "преподаватель"
        Auditor.debug(scheduleTag, "Запрос расписания для $searchType: ${search.id}, кеш: $useCache, требуются данные: $requiresData")
        
        FirebaseCrashlytics.getInstance().setCustomKey("schedule_search_type", searchType)
        FirebaseCrashlytics.getInstance().setCustomKey("schedule_search_id", search.id)

        val lastRequest = db.requestsDao.selectLastOfInstitution(getFilialId())
        val cache: RequestSDBO? = db.requestsDao.selectLastWithData(getFilialId(), search.id)

        return if (
            cache != null
            && useCache
            && cache.request.createdAt == LocalDate.now()
        ) {
            Auditor.debug(scheduleTag, "Расписание загружено из кеша, дата: ${cache.request.createdAt}")
            ScheduleResponse.Available.FromCache(
                cache.daySchedules.map { it.toModel() },
                cache.request.lastModified
            )
        } else {
            Auditor.debug(scheduleTag, "Загрузка расписания из API, последнее изменение: ${cache?.request?.lastModified}, требуется обновление: ${cache == null || requiresData}")

            val fetchSchedule: suspend (String, Boolean, AdditionalData) -> ScheduleResponse =
                if (search is ScheduleSearch.Group) api::getGroupSchedule
                else api::getTeacherSchedule

            val response = fetchSchedule(
                search.id, true, mapOf(
                    "lastModified" to lastRequest?.lastModified,
                    "requiresData" to (cache == null || requiresData)
                )
            )

            if (response is ScheduleResponse.Available) {
                Auditor.debug(scheduleTag, "Расписание успешно получено, сохранение в БД. Дней: ${response.schedules.size}")
                scope.launchIO {
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
            } else if (response is ScheduleResponse.UpToDate) {
                Auditor.debug(scheduleTag, "Расписание актуально, обновление не требуется")
            } else {
                Auditor.debug(scheduleTag, "Расписание пустое")
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
        val dbTag = buildTag(LogScope.DATABASE, LogCat.DB)
        Auditor.debug(dbTag, "Сохранение расписания в БД: запрос=$query, дней=${daySchedules.size}, последнее изменение=$lastModified")
        
        db.withTransaction {
            val requestId = db.requestsDao.insert(
                RequestDBO(
                    institutionId = institutionId,
                    query = query,
                    lastModified = lastModified
                )
            )
            Auditor.debug(dbTag, "Создан запрос с ID: $requestId")

            daySchedules.forEach { daySchedule ->
                val dayScheduleId = db.daySchedulesDao.insert(
                    DayScheduleDBO(
                        fromRequest = requestId,
                        date = daySchedule.date,
                        scheduleType = daySchedule.scheduleType
                    )
                )

                daySchedule.lessons.forEach { lesson ->
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

                    lesson.otUnits.forEach { otUnit ->
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

                        val teacherId = db.teachersDao
                            .selectIdByTeacherId(institutionId, otUnit.teacher.id)
                            ?: db.teachersDao.insert(
                                TeacherDBO(
                                    institutionId = institutionId,
                                    name = otUnit.teacher.name,
                                    teacherId = otUnit.teacher.id
                                )
                            )

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
        
        val totalLessons = daySchedules.sumOf { it.lessons.size }
        Auditor.debug(dbTag, "Расписание сохранено: дней=${daySchedules.size}, уроков=$totalLessons")
    }
}


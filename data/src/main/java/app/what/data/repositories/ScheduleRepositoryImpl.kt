package app.what.data.repositories

import androidx.room.withTransaction
import app.what.domain.models.*
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.Analytics
import app.what.foundation.utils.LogCat
import app.what.foundation.utils.LogScope
import app.what.foundation.utils.buildTag
import app.what.foundation.utils.orThrow
import app.what.schedule.data.local.database.*
import app.what.schedule.data.remote.api.AdditionalData
import app.what.schedule.data.remote.api.InstitutionManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDateTime

class ScheduleRepositoryImpl(
    private val db: AppDatabase,
    private val institutionManager: InstitutionManager,
    private val scope: CoroutineScope
) : app.what.domain.repositories.ScheduleRepository {
    private val api
        get() = institutionManager.getSavedInstitution().orThrow { "No provider selected" }
    
    private fun getFilialId() = api.metadata.id
    
    override suspend fun toggleFavorites(value: ScheduleSearch.Group) {
        val dbTag = buildTag(LogScope.DATABASE, LogCat.DB)
        Auditor.debug(dbTag, "Переключение избранного для группы: ${value.id}")
        
        db.withTransaction {
            val group = db.groupsDao.selectByGroupId(getFilialId(), value.id)!!
            val newFavoriteState = !group.favorite
            db.groupsDao.update(group.copy(favorite = newFavoriteState))
            Auditor.debug(
                dbTag,
                "Группа ${value.id} теперь ${if (newFavoriteState) "в избранном" else "не в избранном"}"
            )
        }
    }
    
    override suspend fun toggleFavorites(value: ScheduleSearch.Teacher) {
        val dbTag = buildTag(LogScope.DATABASE, LogCat.DB)
        Auditor.debug(dbTag, "Переключение избранного для преподавателя: ${value.id}")
        
        db.withTransaction {
            val teacher = db.teachersDao.selectByTeacherId(getFilialId(), value.id)!!
            val newFavoriteState = !teacher.favorite
            db.teachersDao.update(teacher.copy(favorite = newFavoriteState))
            Auditor.debug(
                dbTag,
                "Преподаватель ${value.id} теперь ${if (newFavoriteState) "в избранном" else "не в избранном"}"
            )
        }
    }
    
    override suspend fun getGroups(): List<Group> {
        val dbTag = buildTag(LogScope.DATABASE, LogCat.DB)
        val groups = db.groupsDao
            .selectByInstitution(getFilialId())
            .map { it.toModel() }
            .distinctBy { it.name.trim() }
        
        return if (groups.isEmpty()) {
            Auditor.debug(dbTag, "Группы не найдены в БД, загрузка из API")
            val institutionId = getFilialId()
            val fetched = api.scheduleService.getGroups()
            val seenNames = mutableSetOf<String>()
            val seenIds = mutableSetOf<String>()
            val uniqueFetched = mutableListOf<Group>()
            for (g in fetched) {
                val trimmedName = g.name.trim()
                val id = if (g.id.trim().isNotEmpty()) g.id.trim() else trimmedName
                if (trimmedName.isNotEmpty() && seenNames.add(trimmedName) && seenIds.add(id)) {
                    uniqueFetched.add(g.copy(name = trimmedName, id = id))
                }
            }
            val toInsert = uniqueFetched.map {
                GroupDBO(
                    institutionId = institutionId,
                    name = it.name,
                    groupId = it.id,
                    year = it.year
                )
            }
            db.groupsDao.insert(toInsert)
            Auditor.debug(dbTag, "Загружено групп из API: ${uniqueFetched.size}")
            uniqueFetched
        } else {
            Auditor.debug(dbTag, "Группы загружены из БД: ${groups.size}")
            groups
        }
    }
    
    override suspend fun getTeachers(): List<Teacher> {
        val dbTag = buildTag(LogScope.DATABASE, LogCat.DB)
        val teachers = db.teachersDao
            .selectByInstitution(getFilialId())
            .map { it.toModel() }
            .distinctBy { it.name.trim() }
        
        return if (teachers.isEmpty()) {
            Auditor.debug(dbTag, "Преподаватели не найдены в БД, загрузка из API")
            val institutionId = getFilialId()
            val fetched = api.scheduleService.getTeachers()
            val seenNames = mutableSetOf<String>()
            val seenIds = mutableSetOf<String>()
            val uniqueFetched = mutableListOf<Teacher>()
            for (t in fetched) {
                val trimmedName = t.name.trim()
                val id = if (t.id.trim().isNotEmpty()) t.id.trim() else trimmedName
                if (trimmedName.isNotEmpty() && seenNames.add(trimmedName) && seenIds.add(id)) {
                    uniqueFetched.add(t.copy(name = trimmedName, id = id))
                }
            }
            val toInsert = uniqueFetched.map {
                TeacherDBO(
                    institutionId = institutionId,
                    name = it.name,
                    teacherId = it.id
                )
            }
            db.teachersDao.insert(toInsert)
            Auditor.debug(dbTag, "Загружено преподавателей из API: ${uniqueFetched.size}")
            uniqueFetched
        } else {
            Auditor.debug(dbTag, "Преподаватели загружены из БД: ${teachers.size}")
            teachers
        }
    }
    
    override suspend fun findSearchId(search: ScheduleSearch?): String? = when (search) {
        is ScheduleSearch.Group -> db.groupsDao.selectByGroupId(getFilialId(), search.id)?.groupId
            ?: db.groupsDao.selectByName(getFilialId(), search.name)?.groupId
        
        is ScheduleSearch.Teacher -> db.teachersDao.selectByTeacherId(
            getFilialId(),
            search.id
        )?.teacherId
            ?: db.teachersDao.selectByName(getFilialId(), search.name)?.teacherId
        
        null -> null
    }
    
    override suspend fun getSchedule(
        search: ScheduleSearch,
        useCache: Boolean,
        requiresData: Boolean,
        cloudSync: Boolean
    ): ScheduleResponse {
        Analytics.logScheduleRequest(search.name, search::class.simpleName.toString())
        val scheduleTag = buildTag(LogScope.SCHEDULE, LogCat.DB)
        val searchType = if (search is ScheduleSearch.Group) "группа" else "преподаватель"
        Auditor.debug(
            scheduleTag,
            "Запрос расписания для $searchType: ${search.id}, кеш: $useCache, требуются данные: $requiresData"
        )
        
        val fb = FirebaseCrashlytics.getInstance()
        fb.setCustomKey("schedule_search_type", searchType)
        fb.setCustomKey("schedule_search_id", search.id)
        
        val lastRequest = db.requestsDao.selectLastOfInstitution(getFilialId())
        val cache: RequestSDBO? = db.requestsDao.selectLastWithData(getFilialId(), search.id)

        val additional = mutableMapOf<String, Any?>()
        if (cloudSync && lastRequest != null) {
            additional["lastModified"] = lastRequest.lastModified
        }

        if (useCache && cache != null) {
            Auditor.debug(
                scheduleTag,
                "Возврат данных из кеша: дней=${cache.daySchedules.size}, последнее изменение=${cache.request.lastModified}"
            )
            return ScheduleResponse.Available.FromCache(
                cache.daySchedules.map { it.toModel() },
                cache.request.lastModified
            )
        }

        val netTag = buildTag(LogScope.NETWORK, LogCat.NET)
        Auditor.debug(netTag, "Запрос данных из сети для $searchType: ${search.id}")

        val response = try {
            when (search) {
                is ScheduleSearch.Group -> api.scheduleService.getGroupSchedule(
                    group = search.id,
                    showReplacements = true,
                    additional = additional
                )

                is ScheduleSearch.Teacher -> api.scheduleService.getTeacherSchedule(
                    teacher = search.id,
                    showReplacements = true,
                    additional = additional
                )
            }
        } catch (e: Exception) {
            Auditor.err(netTag, "Ошибка сети при запросе расписания", e)
            val cachedSchedules = cache?.daySchedules?.map { it.toModel() }
            return ScheduleResponse.Error(
                cachedSchedules = cachedSchedules,
                lastModified = cache?.request?.lastModified,
                exception = e
            )
        }

        return when (response) {
            is ScheduleResponse.Available.FromSource -> {
                Auditor.debug(
                    netTag,
                    "Получены новые данные из источника: дней=${response.schedules.size}"
                )
                saveRequest(
                    getFilialId(),
                    search.id,
                    response.lastModified,
                    response.schedules
                )
                response
            }

            is ScheduleResponse.UpToDate -> {
                Auditor.debug(netTag, "Данные актуальны (UpToDate)")
                if (cache != null) {
                    ScheduleResponse.Available.FromCache(
                        cache.daySchedules.map { it.toModel() },
                        cache.request.lastModified
                    )
                } else {
                    ScheduleResponse.Empty
                }
            }

            is ScheduleResponse.Empty -> {
                Auditor.debug(netTag, "Расписание пустое")
                ScheduleResponse.Empty
            }

            is ScheduleResponse.Error -> {
                Auditor.err(netTag, "Ошибка при получении расписания", response.exception)
                response
            }

            is ScheduleResponse.Available.FromCache -> {
                Auditor.debug(netTag, "Получены данные из кеша через API")
                response
            }
        }
    }
    

    private suspend fun saveRequest(
        institutionId: String,
        query: String,
        lastModified: LocalDateTime,
        daySchedules: List<DaySchedule>
    ) {
        val dbTag = buildTag(LogScope.DATABASE, LogCat.DB)
        Auditor.debug(
            dbTag,
            "Сохранение расписания в БД: запрос=$query, дней=${daySchedules.size}, последнее изменение=$lastModified"
        )
        
        db.withTransaction {
            db.requestsDao.deleteAll(getFilialId(), query)
            
            val requestId = db.requestsDao.insert(
                RequestDBO(
                    institutionId = institutionId,
                    query = query,
                    lastModified = lastModified
                )
            )
            Auditor.debug(dbTag, "Создан запрос с ID: $requestId")
            
            for (daySchedule in daySchedules) {
                val dayScheduleId = db.daySchedulesDao.insert(
                    DayScheduleDBO(
                        fromRequest = requestId,
                        date = daySchedule.date,
                        scheduleType = daySchedule.scheduleType
                    )
                )
                
                for (lesson in daySchedule.lessons) {
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
                    
                    val otUnitsToInsert = mutableListOf<OneTimeUnitDBO>()
                    for (otUnit in lesson.otUnits) {
                        val trimmedGroupName = otUnit.group.name.trim()
                        val groupId = db.groupsDao
                            .selectByName(institutionId, trimmedGroupName)?.id
                            ?: db.groupsDao.selectIdByGroupId(institutionId, otUnit.group.id.trim())
                            ?: run {
                                val newId = db.groupsDao.insert(
                                    GroupDBO(
                                        institutionId = institutionId,
                                        name = trimmedGroupName,
                                        groupId = otUnit.group.id.trim(),
                                        year = otUnit.group.year
                                    )
                                )
                                if (newId != -1L) newId
                                else db.groupsDao.selectByName(institutionId, trimmedGroupName)?.id
                                    ?: db.groupsDao.selectIdByGroupId(institutionId, otUnit.group.id.trim())
                                    ?: 0L
                            }
                        
                        val trimmedTeacherName = otUnit.teacher.name.trim()
                        val teacherId = db.teachersDao
                            .selectByName(institutionId, trimmedTeacherName)?.id
                            ?: db.teachersDao.selectIdByTeacherId(institutionId, otUnit.teacher.id.trim())
                            ?: run {
                                val newId = db.teachersDao.insert(
                                    TeacherDBO(
                                        institutionId = institutionId,
                                        name = trimmedTeacherName,
                                        teacherId = otUnit.teacher.id.trim()
                                    )
                                )
                                if (newId != -1L) newId
                                else db.teachersDao.selectByName(institutionId, trimmedTeacherName)?.id
                                    ?: db.teachersDao.selectIdByTeacherId(institutionId, otUnit.teacher.id.trim())
                                    ?: 0L
                            }
                        
                        otUnitsToInsert.add(
                            OneTimeUnitDBO(
                                lessonId = lessonId,
                                groupId = groupId,
                                teacherId = teacherId,
                                auditory = otUnit.auditory,
                                building = otUnit.building
                            )
                        )
                    }
                    if (otUnitsToInsert.isNotEmpty()) {
                        db.otUnitsDao.insert(otUnitsToInsert)
                    }
                }
            }
        }
        
        val totalLessons = daySchedules.sumOf { it.lessons.size }
        Auditor.debug(
            dbTag,
            "Расписание сохранено: дней=${daySchedules.size}, уроков=$totalLessons"
        )
    }
}

package app.what.schedule.data.remote.api

import androidx.compose.runtime.Composable
import app.what.foundation.core.Feature
import app.what.foundation.core.UIComponent
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.remote.api.models.DaySchedule
import app.what.schedule.data.remote.api.models.Group
import app.what.schedule.data.remote.api.models.NewItem
import app.what.schedule.data.remote.api.models.NewListItem
import app.what.schedule.data.remote.api.models.Teacher
import app.what.schedule.data.remote.providers.dgtu.DGTU
import app.what.schedule.data.remote.providers.iubip.IUBIP
import app.what.schedule.data.remote.providers.rinh.RINH
import app.what.schedule.data.remote.providers.rksi.RKSI
import kotlinx.coroutines.CoroutineScope
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
            ScheduleResponse.Empty -> {

            }
            ScheduleResponse.UpToDate -> {

            }
            is ScheduleResponse.Available -> {
                allSchedules.addAll(response.schedules)
                if (latestModified == null || response.lastModified > latestModified) {
                    latestModified = response.lastModified
                }
            }
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


typealias AdditionalData = Map<String, Any?>

interface Institution {
    interface Factory {
        val metadata: MetaInfo
        fun create(): Institution
    }

    val metadata: MetaInfo

    val scheduleService: ScheduleService
    val newsService: NewsService
    
    val accountFeature: Feature<*, *>?

    fun generateFileName(
        additional: AdditionalData,
        fileExtension: String
    ) = "${metadata.id}_${
        additional.map { (k, v) -> "$k&$v" }.joinToString("_")
    }.$fileExtension"
}

interface ScheduleService {
    suspend fun getGroupSchedule(
        group: String,
        showReplacements: Boolean = false,
        additional: AdditionalData = emptyMap()
    ): ScheduleResponse

    suspend fun getTeacherSchedule(
        teacher: String,
        showReplacements: Boolean = false,
        additional: AdditionalData = emptyMap()
    ): ScheduleResponse

    suspend fun getGroups(): List<Group>
    suspend fun getTeachers(): List<Teacher>
}

interface NewsService {
    suspend fun getNews(page: Int): List<NewListItem> = emptyList()
    suspend fun getNewDetail(id: String): NewItem {
        error("Not implemented")
    }
}

interface AccountService {
    val ui: UIComponent
}


val insts: List<Institution.Factory> by lazy {
    listOf(
        RKSI.Factory,
        DGTU.Factory,
        RINH.Factory,
        IUBIP.Factory
    )
}

class InstitutionManager(
    private val settings: AppValues,
    private val scope: CoroutineScope
) {
    init {
        actualize()
    }

    fun getInstitutions(): List<Institution.Factory> = insts

    fun save(institutionId: String) {
        settings.institution.set(institutionId)
        actualize()
    }

    private fun actualize() {
        val savedData = settings.institution.get()
        savedInstitution = insts.firstOrNull { it.metadata.id == savedData }?.create()
    }

    private var savedInstitution: Institution? = null
    fun getSavedInstitution(): Institution? = savedInstitution

    fun reset() {
        savedInstitution = null
        settings.institution.set(null)
    }
}
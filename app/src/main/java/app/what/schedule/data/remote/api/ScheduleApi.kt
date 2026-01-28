package app.what.schedule.data.remote.api

import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.remote.api.models.DaySchedule
import app.what.schedule.data.remote.api.models.Group
import app.what.schedule.data.remote.api.models.NewItem
import app.what.schedule.data.remote.api.models.NewListItem
import app.what.schedule.data.remote.api.models.Teacher
import app.what.schedule.data.remote.providers.DGTU
import app.what.schedule.data.remote.providers.IUBIP
import app.what.schedule.data.remote.providers.RINH
import app.what.schedule.data.remote.providers.RKSI
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


typealias AdditionalData = Map<String, Any?>

interface Institution {
    interface Factory {
        val metadata: MetaInfo
        fun create(): Institution
    }

    val metadata: MetaInfo

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

    suspend fun getNews(page: Int): List<NewListItem> = emptyList()
    suspend fun getNewDetail(id: String): NewItem {
        error("Not implemented")
    }

    fun generateFileName(
        additional: AdditionalData,
        fileExtension: String
    ) = "${metadata.id}_${
        additional.map { (k, v) -> "$k&$v" }.joinToString("_")
    }.$fileExtension"
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
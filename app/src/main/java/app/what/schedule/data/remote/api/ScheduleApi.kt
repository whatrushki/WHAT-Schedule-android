package app.what.schedule.data.remote.api

import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.local.settings.ScheduleProvider
import app.what.schedule.data.remote.api.models.DaySchedule
import app.what.schedule.data.remote.api.models.Group
import app.what.schedule.data.remote.api.models.LessonsSchedule
import app.what.schedule.data.remote.api.models.NewItem
import app.what.schedule.data.remote.api.models.NewListItem
import app.what.schedule.data.remote.api.models.Teacher
import app.what.schedule.data.remote.providers.dgtu.INST_DGTU
import app.what.schedule.data.remote.providers.iubip.INST_IUBIP
import app.what.schedule.data.remote.providers.rinh.INST_RINH
import app.what.schedule.data.remote.providers.rksi.INST_RKSI
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDateTime

data class MetaInfo(
    val id: String,
    val name: String,
    val fullName: String,
    val description: String,
    val sourceTypes: Set<SourceType>,
    val sourceUrl: String,
    val advantages: List<String> = emptyList(),
    val disadvantages: List<String> = emptyList()
) {
    companion object {
        fun empty() = MetaInfo(
            id = "",
            name = "",
            fullName = "",
            description = "",
            sourceTypes = emptySet(),
            sourceUrl = "",
            advantages = emptyList(),
            disadvantages = emptyList()
        )
    }
}

enum class SourceType { API, PARSER, EXCEL, PDF }

data class Institution(
    val metadata: MetaInfo,
    val filials: List<InstitutionFilial>
)

data class InstitutionFilial(
    val metadata: MetaInfo,
    val providers: Set<InstitutionProvider.Factory>,
    val defaultProvider: InstitutionProvider.Factory
)

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

interface InstitutionProvider {
    interface Factory {
        val metadata: MetaInfo
        fun create(): InstitutionProvider
    }

    val metadata: MetaInfo
    val lessonsSchedule: LessonsSchedule

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

val insts by lazy { listOf(INST_RKSI, INST_DGTU, INST_RINH, INST_IUBIP) }

class InstitutionManager(
    private val settings: AppValues,
    private val scope: CoroutineScope
) {
    init {
        actualize()
    }

    fun getInstitutions(): List<Institution> = insts
    fun getProviders(): List<InstitutionProvider> =
        insts.flatMap { it.filials }
            .flatMap { it.providers }
            .map { it.create() }

    fun save(institutionId: String, filialId: String, providerId: String) {
        settings.institution.set(ScheduleProvider(institutionId, filialId, providerId))
        actualize()
    }

    private fun actualize() {
        val savedData = settings.institution.get()
        savedInstitution = insts.firstOrNull { it.metadata.id == savedData?.inst }
        savedFilial = savedInstitution?.filials?.firstOrNull { it.metadata.id == savedData?.filial }
        (savedFilial
            ?.providers
            ?.firstOrNull { it.metadata.id == savedData?.provider }
            ?: savedFilial?.defaultProvider)
            ?.let {
                if (savedProviderFactory == it) return
                savedProviderFactory = it
                savedProvider = it.create()
            }
    }

    private var savedInstitution: Institution? = null
    private var savedFilial: InstitutionFilial? = null
    private var savedProviderFactory: InstitutionProvider.Factory? = null
    private var savedProvider: InstitutionProvider? = null

    fun getSavedInstitution(): Institution? = savedInstitution
    fun getSavedFilial(): InstitutionFilial? = savedFilial
    fun getSavedProvider(): InstitutionProvider? = savedProvider

    fun reset() {
        savedFilial = null
        savedInstitution = null
        savedProviderFactory = null
        savedProvider = null
        settings.institution.set(null)
    }
}
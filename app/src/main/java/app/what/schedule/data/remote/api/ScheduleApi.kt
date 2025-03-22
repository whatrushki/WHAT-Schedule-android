package app.what.schedule.data.remote.api

import app.what.schedule.data.local.settings.AppSettingsRepository
import app.what.schedule.data.remote.impl.rksi.official.RKSIOfficialProvider
import app.what.schedule.data.remote.impl.rksi.turtle.RKSITurtleProvider

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

typealias AdditionalData = Map<String, String>

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
    ): List<DaySchedule>

    suspend fun getTeacherSchedule(
        teacher: String,
        showReplacements: Boolean = false,
        additional: AdditionalData = emptyMap()
    ): List<DaySchedule>

    suspend fun getGroups(): List<Group>
    suspend fun getTeachers(): List<Teacher>

    fun generateFileName(
        additional: AdditionalData,
        fileExtension: String
    ) = "${metadata.id}_${
        additional.map { (k, v) -> "$k&$v" }.joinToString("_")
    }.$fileExtension"
}

val insts by lazy {
    listOf(
        Institution(
            MetaInfo(
                id = "rksi",
                name = "РКСИ",
                fullName = "Ростовский-на-Дону Колледж Связи и Информатики",
                description = "Ростовский-на-Дону Колледж Связи и Информатики",
                sourceTypes = setOf(SourceType.API, SourceType.PARSER, SourceType.EXCEL),
                sourceUrl = "https://rksi.ru"
            ),
            listOf(
                InstitutionFilial(
                    MetaInfo(
                        id = "rksi",
                        name = "РКСИ",
                        fullName = "Ростовский-на-Дону Колледж Связи и Информатики",
                        description = "Ростовский-на-Дону Колледж Связи и Информатики",
                        sourceTypes = setOf(SourceType.API, SourceType.PARSER, SourceType.EXCEL),
                        sourceUrl = "https://rksi.ru"
                    ),
                    setOf(
                        RKSIOfficialProvider.Factory,
                        RKSITurtleProvider.Factory
                    ),
                    RKSIOfficialProvider.Factory
                )
            )
        )
    )
}

class InstitutionManager(
    private val settings: AppSettingsRepository
) {
    init {
        actualize()
    }

    fun getInstitutions(): List<Institution> = insts

    fun save(institutionId: String, filialId: String, providerId: String) {
        settings.setInstitutionData(Triple(institutionId, filialId, providerId))
        actualize()
    }

    private fun actualize() {
        val savedData = settings.getInstitutionData()
        savedInstitution = insts.firstOrNull { it.metadata.id == savedData?.first }
        savedFilial = savedInstitution?.filials?.firstOrNull { it.metadata.id == savedData?.second }
        savedProvider = (savedFilial?.providers?.firstOrNull { it.metadata.id == savedData?.third }
            ?: savedFilial?.defaultProvider)?.create()
    }

    private var savedInstitution: Institution? = null
    private var savedFilial: InstitutionFilial? = null
    private var savedProvider: InstitutionProvider? = null
    fun getSavedInstitution(): Institution? = savedInstitution
    fun getSavedFilial(): InstitutionFilial? = savedFilial
    fun getSavedProvider(): InstitutionProvider? = savedProvider
}
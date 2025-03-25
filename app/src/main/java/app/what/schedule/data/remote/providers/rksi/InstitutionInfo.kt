package app.what.schedule.data.remote.providers.rksi

import app.what.schedule.data.remote.api.Institution
import app.what.schedule.data.remote.api.InstitutionFilial
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.providers.rksi.general.RKSIOfficialProvider
import app.what.schedule.data.remote.providers.rksi.general.RKSITurtleProvider

val INST_RKSI by lazy {
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
}
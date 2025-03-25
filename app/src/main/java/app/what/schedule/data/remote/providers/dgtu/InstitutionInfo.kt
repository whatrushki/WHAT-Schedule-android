package app.what.schedule.data.remote.providers.dgtu

import app.what.schedule.data.remote.api.Institution
import app.what.schedule.data.remote.api.InstitutionFilial
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.providers.dgtu.general.DGTUOfficialProvider

val INST_DGTU by lazy {
    Institution(
        MetaInfo(
            id = "dgtu",
            name = "ДГТУ",
            fullName = "Донской Государственный Технический Университет",
            description = "Донской Государственный Технический Университет",
            sourceTypes = setOf(SourceType.API),
            sourceUrl = "https://edu.donstu.ru/WebApp/#/Rasp"
        ),
        listOf(
            InstitutionFilial(
                MetaInfo(
                    id = "dgtu",
                    name = "ДГТУ",
                    fullName = "Донской Государственный Технический Университет",
                    description = "Донской Государственный Технический Университет",
                    sourceTypes = setOf(SourceType.API),
                    sourceUrl = "https://edu.donstu.ru/WebApp/#/Rasp"
                ),
                setOf(DGTUOfficialProvider.Factory),
                DGTUOfficialProvider.Factory
            )
        )
    )
}
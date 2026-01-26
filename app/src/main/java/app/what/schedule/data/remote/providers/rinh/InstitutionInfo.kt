package app.what.schedule.data.remote.providers.rinh

import app.what.schedule.data.remote.api.Institution
import app.what.schedule.data.remote.api.InstitutionFilial
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.providers.rinh.general.RINHDigitalProvider

val INST_RINH by lazy {
    Institution(
        MetaInfo(
            id = "rinh",
            name = "РГЭУ (РИНХ)",
            fullName = "Ростовский Государственный Экономический Университет",
            description = "Ростовский Государственный Экономический Университет",
            sourceTypes = setOf(SourceType.API),
            sourceUrl = "https://rsue.ru/"
        ),
        listOf(
            InstitutionFilial(
                MetaInfo(
                    id = "rinh_general",
                    name = "РГЭУ (РИНХ) Главный",
                    fullName = "Ростовский Государственный Экономический Университет",
                    description = "Ростовский Государственный Экономический Университет",
                    sourceTypes = setOf(SourceType.API),
                    sourceUrl = "https://rsue.ru/"
                ),
                setOf(RINHDigitalProvider.Factory),
                RINHDigitalProvider.Factory
            )
        )
    )
}
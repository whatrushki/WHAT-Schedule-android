package app.what.schedule.data.remote.providers.iubip

import app.what.schedule.data.remote.api.Institution
import app.what.schedule.data.remote.api.InstitutionFilial
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.providers.iubip.general.IUBIPOfficialProvider

val INST_IUBIP by lazy {
    Institution(
        MetaInfo(
            id = "iubip",
            name = "ИУБиП",
            fullName = "Южный Университет (Институт Управления, Бизнеса и Права)",
            description = "Южный Университет (Институт Управления, Бизнеса и Права)",
            sourceTypes = setOf(SourceType.API, SourceType.PARSER),
            sourceUrl = "https://www.iubip.ru/"
        ),
        listOf(
            InstitutionFilial(
                MetaInfo(
                    id = "iubip",
                    name = "ИУБиП",
                    fullName = "Южный Университет (Институт Управления, Бизнеса и Права)",
                    description = "Южный Университет (Институт Управления, Бизнеса и Права)",
                    sourceTypes = setOf(SourceType.API, SourceType.PARSER),
                    sourceUrl = "https://www.iubip.ru/"
                ),
                setOf(
                    IUBIPOfficialProvider.Factory
                ),
                IUBIPOfficialProvider.Factory
            )
        )
    )
}
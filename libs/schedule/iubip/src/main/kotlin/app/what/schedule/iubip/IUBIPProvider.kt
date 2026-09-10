package app.what.schedule.iubip

import app.what.schedule.core.clients.InstitutionProvider
import app.what.schedule.core.clients.NewsClient
import app.what.schedule.core.clients.ScheduleClient
import app.what.schedule.core.models.InstitutionMetaDto
import app.what.schedule.core.models.SourceTypeDto
import io.ktor.client.HttpClient

class IUBIPProvider(
    private val client: HttpClient,
    private val baseUrl: String = "https://www.iubip.ru",
    private val log: ((String) -> Unit)? = null
) : InstitutionProvider {

    override val metadata = InstitutionMetaDto(
        id = "iubip",
        name = "ИУБиП",
        fullName = "Южный Университет (Институт Управления, Бизнеса и Права)",
        description = "Южный Университет (Институт Управления, Бизнеса и Права)",
        sourceTypes = setOf(SourceTypeDto.API, SourceTypeDto.PARSER),
        sourceUrl = "https://iubip.ru/schedule/"
    )

    override val scheduleClient: ScheduleClient by lazy {
        IUBIPScheduleClient(client, baseUrl, log)
    }

    override val newsClient: NewsClient by lazy {
        IUBIPNewsClient(client, baseUrl, log)
    }
}

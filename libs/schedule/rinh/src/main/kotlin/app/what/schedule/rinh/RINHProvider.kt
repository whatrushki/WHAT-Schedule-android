package app.what.schedule.rinh

import app.what.schedule.core.clients.InstitutionProvider
import app.what.schedule.core.clients.NewsClient
import app.what.schedule.core.clients.ScheduleClient
import app.what.schedule.core.models.InstitutionMetaDto
import app.what.schedule.core.models.SourceTypeDto
import io.ktor.client.HttpClient

class RINHProvider(
    private val client: HttpClient,
    private val scheduleBaseUrl: String = "https://rasp-api.rsue.ru/api",
    private val newsBaseUrl: String = "https://rsue.ru",
    private val log: ((String) -> Unit)? = null
) : InstitutionProvider {

    override val metadata: InstitutionMetaDto = InstitutionMetaDto(
        id = "rinh",
        name = "РИНХ",
        fullName = "Ростовский Государственный Экономический Университет",
        description = "Ростовский Государственный Экономический Университет",
        sourceTypes = setOf(SourceTypeDto.API),
        sourceUrl = "https://rasp.rsue.ru"
    )

    override val scheduleClient: ScheduleClient by lazy {
        RINHScheduleClient(client, scheduleBaseUrl, log)
    }

    override val newsClient: NewsClient by lazy {
        RINHNewsClient(client, newsBaseUrl, log)
    }
}

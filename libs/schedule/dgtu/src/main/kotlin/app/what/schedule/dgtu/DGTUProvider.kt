package app.what.schedule.dgtu

import app.what.schedule.core.clients.InstitutionProvider
import app.what.schedule.core.clients.NewsClient
import app.what.schedule.core.clients.ScheduleClient
import app.what.schedule.core.models.InstitutionMetaDto
import app.what.schedule.core.models.SourceTypeDto
import io.ktor.client.HttpClient

class DGTUProvider(
    client: HttpClient,
    scheduleBaseUrl: String = "https://edu.donstu.ru/api",
    newsBaseUrl: String = "https://donstu.ru",
    accountBaseUrl: String = "https://lk.donstu.ru/api",
    log: ((String) -> Unit)? = null
) : InstitutionProvider {

    override val metadata: InstitutionMetaDto = InstitutionMetaDto(
        id = "dgtu",
        name = "ДГТУ",
        fullName = "Донской государственный технический университет",
        description = "Донской государственный технический университет",
        sourceTypes = setOf(SourceTypeDto.API),
        sourceUrl = "https://edu.donstu.ru",
        hasAccountService = true
    )

    override val scheduleClient: ScheduleClient = DGTUScheduleClient(
        client = client,
        baseUrl = scheduleBaseUrl,
        log = log
    )

    override val newsClient: NewsClient = DGTUNewsClient(
        client = client,
        baseUrl = newsBaseUrl
    )

    val accountClient: DGTUAccountClient = DGTUAccountClient(
        client = client,
        accountBaseUrl = accountBaseUrl
    )
}

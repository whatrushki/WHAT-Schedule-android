package app.what.schedule.rksi

import app.what.schedule.core.cache.FileCache
import app.what.schedule.core.cache.NoOpFileCache
import app.what.schedule.core.clients.InstitutionProvider
import app.what.schedule.core.clients.NewsClient
import app.what.schedule.core.clients.ScheduleClient
import app.what.schedule.core.models.InstitutionMetaDto
import app.what.schedule.core.models.SourceTypeDto
import io.ktor.client.HttpClient

class RKSIProvider(
    client: HttpClient,
    fileCache: FileCache = NoOpFileCache(),
    baseUrl: String = "https://www.rksi.ru",
    log: ((String) -> Unit)? = null
) : InstitutionProvider {

    override val metadata: InstitutionMetaDto = InstitutionMetaDto(
        id = "rksi",
        name = "РКСИ",
        fullName = "Ростовский-на-Дону Колледж Связи и Информатики",
        description = "Ростовский-на-Дону Колледж Связи и Информатики",
        sourceTypes = setOf(SourceTypeDto.PARSER, SourceTypeDto.EXCEL),
        sourceUrl = "https://rksi.ru/mobile_schedule",
        hasAccountService = false
    )

    override val scheduleClient: ScheduleClient = RKSIScheduleClient(
        client = client,
        baseUrl = baseUrl,
        fileCache = fileCache,
        log = log
    )

    override val newsClient: NewsClient = RKSINewsClient(
        client = client,
        baseUrl = baseUrl
    )
}

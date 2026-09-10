package app.what.schedule.data.remote.providers.rksi

import app.what.data.adapters.AdaptedNewsService
import app.what.data.adapters.AdaptedScheduleService
import app.what.data.cache.AndroidFileCache
import app.what.domain.models.MetaInfo
import app.what.domain.models.SourceType
import app.what.foundation.core.Feature
import app.what.schedule.data.remote.api.*
import app.what.schedule.rksi.RKSIProvider
import io.ktor.client.HttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

private val RKSIMetadata
    get() = MetaInfo(
        id = "rksi",
        name = "РКСИ",
        fullName = "Ростовский-на-Дону Колледж Связи и Информатики",
        description = "Ростовский-на-Дону Колледж Связи и Информатики",
        sourceTypes = setOf(SourceType.PARSER, SourceType.EXCEL),
        sourceUrl = "https://rksi.ru/mobile_schedule"
    )

class RKSI(
    provider: RKSIProvider
) : Institution {
    companion object Factory : Institution.Factory, KoinComponent {
        override fun create(): Institution {
            val client: HttpClient = get()
            val fileCache = AndroidFileCache(get())
            val provider = RKSIProvider(client, fileCache, xlsxReader = app.what.schedule.rksi.parser.JvmXlsxReader())
            return RKSI(provider)
        }
        override val metadata: MetaInfo by lazy { RKSIMetadata }
    }

    override val metadata: MetaInfo = Factory.metadata
    override val scheduleService: ScheduleService = AdaptedScheduleService(provider.scheduleClient)
    override val newsService: NewsService = app.what.data.remote.providers.rksi.services.RKSINewsService("https://www.rksi.ru", get())
    override val accountFeature: Feature<*, *>? = null
}
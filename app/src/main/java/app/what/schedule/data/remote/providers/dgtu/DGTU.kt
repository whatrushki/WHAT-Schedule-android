package app.what.schedule.data.remote.providers.dgtu

import app.what.foundation.core.Feature
import app.what.schedule.data.remote.api.Institution
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.NewsService
import app.what.schedule.data.remote.api.ScheduleService
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.providers.dgtu.services.DGTUNewsService
import app.what.schedule.data.remote.providers.dgtu.services.DGTUScheduleService
import app.what.schedule.features.insts.dgtu.DgtuFeature
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

private val DGTUProviderMetadata
    get() = MetaInfo(
        id = "dgtu",
        name = "ДГТУ",
        fullName = "Донской Государственный Технический Университет",
        description = "Донской Государственный Технический Университет",
        sourceTypes = setOf(SourceType.API),
        sourceUrl = "https://edu.donstu.ru/WebApp/#/Rasp",
    )

class DGTU(
    client: HttpClient,
    scope: CoroutineScope,
) : Institution {
    companion object Factory : Institution.Factory, KoinComponent {
        private const val SCHEDULE_BASE_URL = "https://edu.donstu.ru/api"
        private const val NEWS_BASE_URL = "https://news.donstu.ru"
        
        override val metadata by lazy { DGTUProviderMetadata }
        override fun create() = DGTU(get(), get())
    }
    
    override val metadata = Factory.metadata
    override val scheduleService: ScheduleService =
        DGTUScheduleService(SCHEDULE_BASE_URL, client, scope)
    override val newsService: NewsService =
        DGTUNewsService(NEWS_BASE_URL, client)
    override val accountFeature: Feature<*, *> = DgtuFeature()
}

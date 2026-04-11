package app.what.schedule.data.remote.providers.rinh

import app.what.foundation.core.Feature
import app.what.schedule.data.remote.api.Institution
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.NewsService
import app.what.schedule.data.remote.api.ScheduleService
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.providers.rinh.services.RINHNewsService
import app.what.schedule.data.remote.providers.rinh.services.RINHScheduleService
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

private val RINHMetadata
    get() = MetaInfo(
        id = "rinh",
        name = "РИНХ",
        fullName = "Ростовский Государственный Экономический Университет",
        description = "Ростовский Государственный Экономический Университет",
        sourceTypes = setOf(SourceType.API),
        sourceUrl = "https://rasp.rsue.ru",
    )

class RINH(
    client: HttpClient,
    scope: CoroutineScope
) : Institution {
    companion object Factory : Institution.Factory, KoinComponent {
        private const val SCHEDULE_BASE_URL = "https://rasp-api.rsue.ru/api"
        private const val NEWS_BASE_URL = "https://rsue.ru"
        override val metadata by lazy { RINHMetadata }
        override fun create() = RINH(get(), get())
    }
    
    override val metadata = Factory.metadata
    override val scheduleService: ScheduleService =
        RINHScheduleService(SCHEDULE_BASE_URL, client, scope)
    override val newsService: NewsService =
        RINHNewsService(NEWS_BASE_URL, client)
    override val accountFeature: Feature<*, *>? = null
}




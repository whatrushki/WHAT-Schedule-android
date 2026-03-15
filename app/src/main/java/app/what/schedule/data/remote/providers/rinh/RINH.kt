package app.what.schedule.data.remote.providers.rinh

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import app.what.foundation.core.Feature
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.asyncLazy
import app.what.schedule.data.remote.api.AccountService
import app.what.schedule.data.remote.api.AdditionalData
import app.what.schedule.data.remote.api.Institution
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.NewsService
import app.what.schedule.data.remote.api.ScheduleResponse
import app.what.schedule.data.remote.api.ScheduleService
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.api.models.DaySchedule
import app.what.schedule.data.remote.api.models.Group
import app.what.schedule.data.remote.api.models.Lesson
import app.what.schedule.data.remote.api.models.LessonState
import app.what.schedule.data.remote.api.models.LessonType
import app.what.schedule.data.remote.api.models.LessonsScheduleType
import app.what.schedule.data.remote.api.models.NewContent
import app.what.schedule.data.remote.api.models.NewItem
import app.what.schedule.data.remote.api.models.NewListItem
import app.what.schedule.data.remote.api.models.NewTag
import app.what.schedule.data.remote.api.models.OneTimeUnit
import app.what.schedule.data.remote.api.models.Teacher
import app.what.schedule.data.remote.providers.rinh.services.RINHNewsService
import app.what.schedule.data.remote.providers.rinh.services.RINHScheduleService
import app.what.schedule.data.remote.utils.parseMonth
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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




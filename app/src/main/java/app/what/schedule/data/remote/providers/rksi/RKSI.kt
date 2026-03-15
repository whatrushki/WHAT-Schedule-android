package app.what.schedule.data.remote.providers.rksi

import app.what.foundation.core.Feature
import app.what.schedule.data.remote.api.Institution
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.NewsService
import app.what.schedule.data.remote.api.ScheduleService
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.api.models.LessonTime
import app.what.schedule.data.remote.api.models.LessonType
import app.what.schedule.data.remote.providers.rksi.services.RKSINewsService
import app.what.schedule.data.remote.providers.rksi.services.RKSIScheduleService
import app.what.schedule.libs.FileManager
import app.what.schedule.libs.GoogleDriveParser
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.LocalTime


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
    client: HttpClient,
    googleDriveApi: GoogleDriveParser,
    fileManager: FileManager,
    scope: CoroutineScope
) : Institution {
    companion object Factory : Institution.Factory, KoinComponent {
        private const val BASE_URL = "https://www.rksi.ru"
        override fun create() = RKSI(get(), get(), get(), get())
        override val metadata: MetaInfo by lazy { RKSIMetadata }
    }

    override val metadata: MetaInfo = Factory.metadata
    override val scheduleService: ScheduleService =
        RKSIScheduleService(
            BASE_URL,
            client,
            googleDriveApi,
            fileManager,
            scope,
            ::generateFileName
        )
    override val newsService: NewsService = RKSINewsService(BASE_URL, client)
    override val accountFeature: Feature<*, *>? = null
}

object RKSILessonsSchedule {
    val COMMON = listOf(
        LessonTime(1, LocalTime.of(8, 0), LocalTime.of(9, 30)),
        LessonTime(2, LocalTime.of(9, 40), LocalTime.of(11, 10)),
        LessonTime(3, LocalTime.of(11, 30), LocalTime.of(13, 0)),
        LessonTime(4, LocalTime.of(13, 10), LocalTime.of(14, 40)),
        LessonTime(5, LocalTime.of(15, 0), LocalTime.of(16, 30)),
        LessonTime(6, LocalTime.of(16, 40), LocalTime.of(18, 10)),
        LessonTime(7, LocalTime.of(18, 20), LocalTime.of(19, 50))
    )

    val SHORTENED = listOf(
        LessonTime(1, LocalTime.of(8, 0), LocalTime.of(8, 50)),
        LessonTime(2, LocalTime.of(9, 0), LocalTime.of(9, 50)),
        LessonTime(3, LocalTime.of(10, 0), LocalTime.of(10, 50)),
        LessonTime(4, LocalTime.of(11, 0), LocalTime.of(11, 50)),
        LessonTime(5, LocalTime.of(12, 0), LocalTime.of(12, 50)),
        LessonTime(6, LocalTime.of(13, 0), LocalTime.of(13, 50)),
        LessonTime(7, LocalTime.of(14, 0), LocalTime.of(14, 50))
    )

    val WITH_CLASS_HOUR = listOf(
        LessonTime(1, LocalTime.of(8, 0), LocalTime.of(9, 30)),
        LessonTime(2, LocalTime.of(9, 40), LocalTime.of(11, 10)),
        LessonTime(3, LocalTime.of(11, 30), LocalTime.of(13, 0)),
        LessonTime(
            number = 0,
            LocalTime.of(13, 5),
            LocalTime.of(14, 5),
            type = LessonType.CLASS_HOUR
        ),
        LessonTime(4, LocalTime.of(14, 10), LocalTime.of(15, 40)),
        LessonTime(5, LocalTime.of(16, 0), LocalTime.of(17, 30)),
        LessonTime(6, LocalTime.of(17, 40), LocalTime.of(19, 10))
    )
}
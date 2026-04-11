package app.what.schedule.data.remote.providers.iubip

import app.what.foundation.core.Feature
import app.what.schedule.data.remote.api.Institution
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.NewsService
import app.what.schedule.data.remote.api.ScheduleService
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.api.models.LessonTime
import app.what.schedule.data.remote.providers.iubip.services.IUBIPNewsService
import app.what.schedule.data.remote.providers.iubip.services.IUBIPScheduleService
import io.ktor.client.HttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.LocalTime

private val IUBIPMetadata
    get() = MetaInfo(
        id = "iubip",
        name = "ИУБиП",
        fullName = "Южный Университет (Институт Управления, Бизнеса и Права)",
        description = "Южный Университет (Институт Управления, Бизнеса и Права)",
        sourceTypes = setOf(SourceType.API, SourceType.PARSER),
        sourceUrl = "https://iubip.ru/schedule/",
    )

class IUBIP(
    client: HttpClient
) : Institution {
    companion object Factory : Institution.Factory, KoinComponent {
        private const val BASE_URL = "https://www.iubip.ru"
        
        override val metadata by lazy { IUBIPMetadata }
        override fun create() = IUBIP(get())
    }
    
    override val metadata = Factory.metadata
    
    override val scheduleService: ScheduleService =
        IUBIPScheduleService(BASE_URL, client)
    override val newsService: NewsService =
        IUBIPNewsService(BASE_URL, client)
    override val accountFeature: Feature<*, *>? = null
}

object IUBIPLessonsSchedule {
    val COMMON = listOf(
        LessonTime(1, LocalTime.of(8, 20), LocalTime.of(9, 50)),
        LessonTime(2, LocalTime.of(10, 0), LocalTime.of(11, 30)),
        LessonTime(3, LocalTime.of(11, 40), LocalTime.of(13, 10)),
        LessonTime(4, LocalTime.of(13, 30), LocalTime.of(15, 0)),
        LessonTime(5, LocalTime.of(15, 10), LocalTime.of(16, 40)),
        LessonTime(6, LocalTime.of(17, 0), LocalTime.of(18, 30)),
        LessonTime(7, LocalTime.of(18, 40), LocalTime.of(20, 10)),
        LessonTime(8, LocalTime.of(20, 20), LocalTime.of(21, 50))
    )
}
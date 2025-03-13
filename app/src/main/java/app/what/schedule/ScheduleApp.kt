package app.what.schedule

import android.app.Application
import app.what.schedule.data.local.settings.AppSettingsRepository
import app.what.schedule.data.remote.api.ScheduleApi
import app.what.schedule.data.remote.impl.rksi.FileManager
import app.what.schedule.data.remote.impl.rksi.GoogleDriveApi
import app.what.schedule.data.remote.impl.rksi.ReplacementsRepository
import app.what.schedule.data.remote.impl.rksi.official.RKSIScheduleApi
import app.what.schedule.data.remote.impl.rksi.turtle.TurtleScheduleApi
import app.what.schedule.features.main.domain.MainController
import app.what.schedule.features.schedule.domain.ScheduleController
import app.what.schedule.features.settings.domain.SettingsController
import app.what.schedule.utils.AppUtils
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class ScheduleApp : Application() {
    override fun onCreate() {
        super.onCreate()


        startKoin {
            androidContext(this@ScheduleApp)
            modules(generalModule)
        }
    }
}

val generalModule = module {
    single<SettingsController> { SettingsController(get(), get()) }
    single<ScheduleController> { ScheduleController(get(), get()) }
    single<MainController> { MainController() }
    single { ReplacementsRepository(get(), get(), get()) }
    single<ScheduleApi> {
        val server = get<AppSettingsRepository>().getUsedServer()
        when (server) {
            AppSettingsRepository.AppServers.TURTLE -> TurtleScheduleApi(get(), get())
            AppSettingsRepository.AppServers.RKSI -> RKSIScheduleApi(get(), get())
        }
    }

    single { AppSettingsRepository(get()) }
    single { AppUtils(get()) }
    single { GoogleDriveApi(get()) }
    single { FileManager(get()) }

    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}
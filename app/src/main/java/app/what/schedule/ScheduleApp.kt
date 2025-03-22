package app.what.schedule

import android.app.Application
import androidx.room.Room
import app.what.schedule.data.local.database.AppDatabase
import app.what.schedule.data.local.settings.AppSettingsRepository
import app.what.schedule.data.remote.api.InstitutionManager
import app.what.schedule.features.main.domain.MainController
import app.what.schedule.features.onboarding.domain.OnboardingController
import app.what.schedule.features.schedule.domain.ScheduleController
import app.what.schedule.features.settings.domain.SettingsController
import app.what.schedule.libs.FileManager
import app.what.schedule.libs.GoogleDriveParser
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
    single { AppSettingsRepository(get()) }
    single { AppUtils(get()) }
    single { GoogleDriveParser(get()) }
    single { FileManager(get()) }

    single<SettingsController> { SettingsController(get(), get()) }
    single<ScheduleController> { ScheduleController(get(), get()) }
    single<OnboardingController> { OnboardingController(get(), get()) }
    single<MainController> { MainController() }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "schedule.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().lessonsDao }
    single { get<AppDatabase>().otUnitsDao }
    single { get<AppDatabase>().groupsDao }
    single { get<AppDatabase>().teachersDao }

    single { InstitutionManager(get()) }

    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
}
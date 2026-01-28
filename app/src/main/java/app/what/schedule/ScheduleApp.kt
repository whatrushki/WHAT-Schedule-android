package app.what.schedule

import android.app.Application
import androidx.room.Room
import app.what.foundation.services.AppLogger
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.services.auto_update.GitHubUpdateService
import app.what.foundation.services.auto_update.UpdateConfig
import app.what.foundation.services.auto_update.UpdateManager
import app.what.foundation.services.crash.CrashHandler
import app.what.schedule.data.local.database.AppDatabase
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.remote.api.InstitutionManager
import app.what.schedule.domain.NewsRepository
import app.what.schedule.domain.ScheduleRepository
import app.what.schedule.features.dev.presentation.NetworkMonitorPlugin
import app.what.schedule.features.main.domain.MainController
import app.what.schedule.features.news.domain.NewsController
import app.what.schedule.features.newsDetail.domain.NewsDetailController
import app.what.schedule.features.onboarding.domain.OnboardingController
import app.what.schedule.features.schedule.domain.ScheduleController
import app.what.schedule.features.settings.domain.SettingsController
import app.what.schedule.libs.FileManager
import app.what.schedule.libs.GoogleDriveParser
import app.what.schedule.utils.AppUtils
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class ScheduleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        CrashHandler.initialize(applicationContext, CrashActivity::class.java)
        AppLogger.initialize(applicationContext)
        Auditor.info("core", "App started")

        startKoin {
            androidContext(this@ScheduleApp)
            modules(generalModule, controllers)
        }
    }
}

val controllers = module {
    single<SettingsController> { SettingsController(get(), get()) }
    single<NewsController> { NewsController(get(), get()) }
    factory<NewsDetailController> { params -> NewsDetailController(params.get(), get()) }
    single<ScheduleController> { ScheduleController(get(), get()) }
    single<OnboardingController> { OnboardingController(get(), get()) }
    single<MainController> { MainController() }
}

val generalModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    single { AppValues(get()) }
    single { AppUtils(get()) }
    single { GoogleDriveParser(get()) }
    single { FileManager(get()) }
    single {
        UpdateManager(
            get(), get(), UpdateConfig(
                "whatrushki", "WHAT-Schedule-android",
                BuildConfig.VERSION_NAME
            ), CoroutineScope(IO) // TODO: найти оптимальный scope
        )
    }
    single { GitHubUpdateService(get()) }


    single { InstitutionManager(get(), get()) }
    single { ScheduleRepository(get(), get(), get()) }
    single { NewsRepository(get(), get()) }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "schedule.db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    single {
        HttpClient(CIO) {
            install(NetworkMonitorPlugin)

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Auditor.debug("ktor", message)
                    }
                }
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    classDiscriminator = "type"
                    prettyPrint = true
                })
            }

            install(HttpTimeout) {
                this@HttpClient.expectSuccess = false
                requestTimeoutMillis = 60 * 1000
            }

            engine {
                https {
                    trustManager = object : X509TrustManager {
                        override fun checkClientTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                        ) {
                        }

                        override fun checkServerTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    }
                }
            }
        }
    }
}
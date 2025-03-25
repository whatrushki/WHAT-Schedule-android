package app.what.schedule

import android.app.Application
import android.util.Log
import androidx.room.Room
import app.what.schedule.data.local.database.AppDatabase
import app.what.schedule.data.local.settings.AppSettingsRepository
import app.what.schedule.data.remote.api.InstitutionManager
import app.what.schedule.domain.ScheduleRepository
import app.what.schedule.features.main.domain.MainController
import app.what.schedule.features.onboarding.domain.OnboardingController
import app.what.schedule.features.schedule.domain.ScheduleController
import app.what.schedule.features.settings.domain.SettingsController
import app.what.schedule.libs.FileManager
import app.what.schedule.libs.GoogleDriveParser
import app.what.schedule.utils.AppUtils
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

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

    single { InstitutionManager(get()) }
    single { ScheduleRepository(get(), get()) }

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

    single {
        HttpClient(CIO) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("ktor", message)
                    }
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
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
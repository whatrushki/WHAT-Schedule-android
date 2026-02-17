package app.what.schedule

import android.app.Application
import android.widget.Toast
import androidx.room.Room
import app.what.foundation.services.AppLogger
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.services.auto_update.AppUpdateManager
import app.what.foundation.services.auto_update.GitHubUpdateManager
import app.what.foundation.services.auto_update.GitHubUpdateService
import app.what.foundation.services.auto_update.InstallSource
import app.what.foundation.services.auto_update.RuStoreUpdateManager
import app.what.foundation.services.auto_update.UpdateConfig
import app.what.foundation.services.auto_update.getInstallSource
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
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.security.cert.X509Certificate
import java.util.UUID
import javax.net.ssl.X509TrustManager

class ScheduleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("app_version_code", BuildConfig.VERSION_CODE)

        AppLogger.initialize(applicationContext)
        CrashHandler.initialize(applicationContext, CrashActivity::class.java)
            .setSideEffect(crashlytics::recordException)

        val initTag = buildTag(LogScope.CORE, LogCat.INIT)
        Auditor.info(initTag, "Приложение запущено")

        startKoin {
            androidContext(this@ScheduleApp)
            modules(generalModule, controllers)
        }

        val koin = getKoin()

        val appValues = koin.get<AppValues>()
        if (appValues.userId.get() == null) {
            val userId = UUID.randomUUID().toString()
            crashlytics.setUserId(userId)
            Firebase.analytics.setUserId(userId)
            appValues.userId.set(userId)
            Auditor.debug(initTag, "Создан новый пользователь: $userId")
        } else {
            Auditor.debug(initTag, "Пользователь уже существует: ${appValues.userId.get()}")
        }

        SingletonImageLoader.setSafe {
            ImageLoader.Builder(this)
                .crossfade(true)
                .components {
                    add(KtorNetworkFetcherFactory({ koin.get<HttpClient>() }))
                }
                .build()
        }

        val source = getInstallSource(this)

        when (source) {
            InstallSource.APK -> Auditor.debug("d", "Apk")
            InstallSource.RuStore -> Auditor.debug("d", "RuStore")
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
    single<AppUpdateManager> {

        val context = androidContext()
        val source = getInstallSource(context)

        when (source) {

            InstallSource.RuStore -> RuStoreUpdateManager(context, get())
            InstallSource.APK -> GitHubUpdateManager(
                GitHubUpdateService(get()),
                androidContext(),
                UpdateConfig(
                    BuildConfig.APP_GITHUB_URL.split("/").reversed()[1],
                    BuildConfig.APP_GITHUB_URL.split("/").reversed()[0],
                    BuildConfig.VERSION_NAME
                ),
                get()
            )
        }
    }


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
                        Auditor.debug(buildTag(LogScope.NETWORK, LogCat.NET), message)
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
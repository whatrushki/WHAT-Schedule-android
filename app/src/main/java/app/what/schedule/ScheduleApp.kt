package app.what.schedule

import android.app.Application
import android.widget.Toast
import androidx.room.Room
import app.what.foundation.data.settings.PreferenceStorage
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
import app.what.foundation.utils.launchIO
import app.what.schedule.data.local.database.AppDatabase
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.remote.api.InstitutionManager
import app.what.schedule.data.remote.providers.dgtu.DGTU
import app.what.schedule.data.remote.providers.dgtu.services.DGTUAccountService
import app.what.schedule.domain.NewsRepository
import app.what.schedule.domain.ScheduleRepository
import app.what.schedule.features.dev.presentation.NetworkMonitorPlugin
import app.what.schedule.features.insts.dgtu.domain.DgtuController
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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
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
            InstallSource.APK -> Auditor.debug("d", "install source Apk")
            InstallSource.RuStore -> Auditor.debug("d", "install source RuStore")
        }
    }
}

val controllers = module {
    singleOf(::SettingsController)
    singleOf(::NewsController)
    singleOf(::ScheduleController)
    singleOf(::OnboardingController)
    singleOf(::MainController)
    singleOf(::DgtuController)
    factory<NewsDetailController> { params -> NewsDetailController(params.get(), get()) }
}

val generalModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }

    singleOf(::AppValues) bind PreferenceStorage::class
    singleOf(::AppUtils)
    singleOf(::GoogleDriveParser)
    singleOf(::FileManager)

    singleOf(::DGTUAccountService)

    singleOf(::InstitutionManager)
    singleOf(::ScheduleRepository)
    singleOf(::NewsRepository)

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
                    classDiscriminator = "type"
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                    explicitNulls = false
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
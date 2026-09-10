package app.what.schedule

import android.app.Application
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
import app.what.schedule.data.local.database.AppDatabase
import app.what.data.di.dataModule
import app.what.features.main.di.mainFeatureModule
import app.what.schedule.data.local.settings.AppValues
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
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.util.UUID

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
            modules(dataModule, mainFeatureModule, appModule)
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

val appModule = module {
    singleOf(::AppUtils)
    
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
}
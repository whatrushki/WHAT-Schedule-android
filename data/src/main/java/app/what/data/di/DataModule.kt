package app.what.data.di

import androidx.room.Room
import app.what.data.libs.FileManager
import app.what.data.libs.GoogleDriveParser
import app.what.domain.repositories.NewsRepository
import app.what.domain.repositories.ScheduleRepository
import app.what.foundation.data.settings.PreferenceStorage
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.schedule.data.local.database.AppDatabase
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.remote.api.InstitutionManager
import app.what.data.repositories.ScheduleRepositoryImpl
import app.what.data.repositories.NewsRepositoryImpl
import app.what.schedule.core.cache.FileCache
import app.what.data.cache.AndroidFileCache
import app.what.schedule.dgtu.DGTUAccountClient
import app.what.foundation.utils.LogCat
import app.what.foundation.utils.LogScope
import app.what.foundation.utils.buildTag
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

val dataModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    
    singleOf(::AppValues) bind PreferenceStorage::class
    singleOf(::GoogleDriveParser)
    singleOf(::FileManager)
    single<FileCache> { AndroidFileCache(androidContext()) }
    single { DGTUAccountClient(get()) }
    singleOf(::InstitutionManager)
    
    single<ScheduleRepository> {
        ScheduleRepositoryImpl(get(), get(), get())
    }
    
    single<NewsRepository> {
        NewsRepositoryImpl(get(), get())
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
            install(HttpRequestRetry) {
                maxRetries = 3
                retryOnExceptionIf { _, cause ->
                    cause is java.net.UnknownHostException ||
                            cause is java.net.ConnectException ||
                            cause is java.net.SocketTimeoutException
                }
                retryOnServerErrors(maxRetries = 2)
                exponentialDelay(baseDelayMs = 300L, maxDelayMs = 4000L)
            }
            
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Auditor.debug(buildTag(LogScope.NETWORK, LogCat.NET), message)
                    }
                }
            }
            
            install(app.what.foundation.network.monitor.NetworkMonitorPlugin)
            
            install(ContentNegotiation) {
                json(Json {
                    classDiscriminator = "type"
                    ignoreUnknownKeys = true
                    prettyPrint = false
                    isLenient = true
                    explicitNulls = false
                })
            }
            
            install(HttpTimeout) {
                this@HttpClient.expectSuccess = false
                requestTimeoutMillis = 45 * 1000
                connectTimeoutMillis = 15 * 1000
                socketTimeoutMillis = 30 * 1000
            }
            
            defaultRequest {
                header(HttpHeaders.UserAgent, "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 WHAT-Schedule")
                header(HttpHeaders.Accept, "*/*")
                header(HttpHeaders.AcceptLanguage, "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
            }
            
            engine {
                https {
                    trustManager = object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    }
                }
            }
        }
    }
}

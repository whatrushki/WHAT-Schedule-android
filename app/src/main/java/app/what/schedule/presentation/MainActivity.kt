package app.what.schedule.presentation

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import app.what.foundation.utils.suspendCall
import app.what.navigation.core.NavigationHost
import app.what.navigation.core.ProvideGlobalSheet
import app.what.schedule.data.local.database.AppDatabase
import app.what.schedule.data.local.settings.AppSettingsRepository
import app.what.schedule.features.main.navigation.MainProvider
import app.what.schedule.features.main.navigation.mainRegistry
import app.what.schedule.features.onboarding.navigation.OnboardingProvider
import app.what.schedule.features.onboarding.navigation.onboardingRegistry
import app.what.schedule.presentation.theme.WHATScheduleTheme
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setNavigationBarContrastEnforced(false)
            }

            val db = koinInject<AppDatabase>()
//            val fileManager = koinInject<FileManager>()
//            val client by lazy {
//                HttpClient(CIO) {
//                    install(ContentNegotiation) {
//                        json(Json {
//                            ignoreUnknownKeys = true
//                            isLenient = true
//                        })
//                    }
//                    engine {
//                        https {
//                            trustManager = object : X509TrustManager {
//                                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
//                                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
//                                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
//                            }
//                        }
//                    }
//                }
//            }

            LaunchedEffect(Unit) {
                suspendCall { db.requestsDao.deleteOld() }

//
//                val file = client
//                    .get("https://rk-culture.ru/sites/default/files/docs/pages/%D0%A1%D0%BF%D0%B5%D1%86%D0%B8%D0%B0%D0%BB%D1%8C%D0%BD%D0%BE%D1%81%D1%82%D1%8C%2052.02.04%20%D0%90%D0%BA%D1%82%D0%B5%D1%80%D1%81%D0%BA%D0%BE%D0%B5%20%D0%B8%D1%81%D0%BA%D1%83%D1%81%D1%81%D1%82%D0%B2%D0%BE_3.pdf")
//                    .readRawBytes()
//                    .let {
//                        fileManager.writeFile(
//                            type = FileManager.DirectoryType.PUBLIC,
//                            fileName = "test.pdf", it
//                        )
//                        fileManager.getFile(type = FileManager.DirectoryType.CACHE, "test.pdf")
//                    }
//
//                PDFBoxResourceLoader.init(applicationContext)
//                val doc = PDDocument.load(file)
//                Log.d("d", "doc: ${doc.document.xrefTable}")
//                Log.d("d", "doc: ${doc.document.version}")
//                Log.d("d", "doc: ${doc.document.documentID}")
//                Log.d("d", "doc: ${doc.document.trailer}")
//                Log.d("d", "doc: ${doc.document.startXref}")
//                Log.d("d", "doc: ${doc.pages}")
//                Log.d("d", "doc: ${doc.numberOfPages}")
//                Log.d("d", "doc: ${doc.pages.first().artBox}")
//                Log.d("d", "doc: ${doc.pages.first().trimBox}")
//                Log.d("d", "doc: ${doc.pages.first().cropBox}")
//                Log.d("d", "doc: ${doc.pages.first().actions}")
//                Log.d("d", "doc: ${doc.pages.first().viewports}")
//                Log.d("d", "doc: ${doc.pages.first().mediaBox}")
            }

//            return@setContent

            WHATScheduleTheme {
                ProvideGlobalSheet {
                    val settings = koinInject<AppSettingsRepository>()

                    NavigationHost(
                        start = if (settings.isFirstLaunch()) OnboardingProvider
                        else MainProvider
                    ) {
                        mainRegistry()
                        onboardingRegistry()
                    }
                }
            }
        }
    }
}

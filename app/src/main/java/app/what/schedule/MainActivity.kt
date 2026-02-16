package app.what.schedule

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.navigation.core.NavigationHost
import app.what.navigation.core.ProvideGlobalDialog
import app.what.navigation.core.ProvideGlobalSheet
import app.what.navigation.core.rememberHostNavigator
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.local.settings.ProvideGLobalAppValues
import app.what.schedule.features.main.navigation.MainProvider
import app.what.schedule.features.main.navigation.mainRegistry
import app.what.schedule.features.newsDetail.navigation.newsDetailRegistry
import app.what.schedule.features.onboarding.navigation.OnboardingProvider
import app.what.schedule.features.onboarding.navigation.onboardingRegistry
import app.what.schedule.ui.theme.AppTheme
import app.what.schedule.utils.Analytics
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uiTag = buildTag(LogScope.UI, LogCat.INIT)
        Auditor.info(uiTag, "MainActivity создана")

        enableEdgeToEdge()
        setContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setNavigationBarContrastEnforced(false)
            }

            val navigator = rememberHostNavigator()
            val settings = koinInject<AppValues>()

            LaunchedEffect(Unit) {
                navigator.c.addOnDestinationChangedListener { _, destination, _ ->
                    val navTag = buildTag(LogScope.CORE, LogCat.NAV)
                    Auditor.debug(navTag, "Навигация: ${destination.route}")
                    Analytics.logScreenView(destination.route ?: "no route")
                    crashlytics.setCustomKey("current_screen", destination.route ?: "unknown")
                }
            }

            ProvideGLobalAppValues(settings) {
                AppTheme {
                    ProvideGlobalDialog {
                        ProvideGlobalSheet {
                            NavigationHost(
                                start = if (settings.isFirstLaunch.get()!!) OnboardingProvider
                                else MainProvider
                            ) {
                                mainRegistry()
                                newsDetailRegistry()
                                onboardingRegistry()
                            }
                        }
                    }
                }
            }
        }
    }
}

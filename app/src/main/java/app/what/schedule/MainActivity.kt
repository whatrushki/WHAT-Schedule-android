package app.what.schedule

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import app.what.foundation.ui.theme.WHATTheme
import app.what.navigation.core.NavigationHost
import app.what.navigation.core.ProvideGlobalDialog
import app.what.navigation.core.ProvideGlobalSheet
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.features.main.navigation.MainProvider
import app.what.schedule.features.main.navigation.mainRegistry
import app.what.schedule.features.onboarding.navigation.OnboardingProvider
import app.what.schedule.features.onboarding.navigation.onboardingRegistry
import app.what.schedule.features.settings.presentation.ThemeType
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            // НЕ ПЕРЕМЕЩАТЬ!!
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setNavigationBarContrastEnforced(false)
            }

            val settings = koinInject<AppValues>()
            val themeType by settings.themeType.collect()

            WHATTheme(
                darkTheme = when (themeType) {
                    ThemeType.Dark -> true
                    ThemeType.System -> isSystemInDarkTheme()
                    else -> false
                }
            ) {
                ProvideGlobalDialog {
                    ProvideGlobalSheet {
                        NavigationHost(
                            start = if (settings.isFirstLaunch.get()!!) OnboardingProvider
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
}
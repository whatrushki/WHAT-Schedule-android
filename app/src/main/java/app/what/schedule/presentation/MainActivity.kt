package app.what.schedule.presentation

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.what.navigation.core.NavigationHost
import app.what.navigation.core.ProvideGlobalSheet
import app.what.schedule.features.main.navigation.MainProvider
import app.what.schedule.features.main.navigation.mainRegistry
import app.what.schedule.presentation.theme.WHATScheduleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                window.setStatusBarContrastEnforced(false)
                window.setNavigationBarContrastEnforced(false)
            }

            WHATScheduleTheme {
                ProvideGlobalSheet {
                    NavigationHost(start = MainProvider) {
                        mainRegistry()
                    }
                }
            }
        }
    }
}

package app.what.schedule.features.dev.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.what.foundation.ui.Gap
import app.what.navigation.core.rememberNavigator
import app.what.schedule.controllers
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.features.onboarding.navigation.OnboardingProvider
import app.what.schedule.ui.components.Fallback
import org.koin.compose.koinInject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

@Composable
fun FeaturePane() = Column {
    val navigator = rememberNavigator()
    val settings = koinInject<AppValues>()
    Button({
        unloadKoinModules(controllers)
        loadKoinModules(controllers)
        settings.lastSearch.set(null)
        navigator.parent!!.c.navigate(OnboardingProvider) {
            popUpTo(0) {
                inclusive = true
            }
        }
    }) {
        Text("Resellect institution")
    }

    Gap(8)

    Fallback(
        text = "В разработке",
        modifier = Modifier.fillMaxSize()
    )
}
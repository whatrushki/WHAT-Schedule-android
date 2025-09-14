package app.what.schedule.features.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import animatedStarsBackground
import app.what.foundation.core.Feature
import app.what.navigation.core.NavComponent
import app.what.navigation.core.NavigationHost
import app.what.navigation.core.Registry
import app.what.navigation.core.bottom_navigation.BottomNavBar
import app.what.navigation.core.bottom_navigation.NavAction
import app.what.navigation.core.bottom_navigation.navItem
import app.what.navigation.core.rememberHostNavigator
import app.what.schedule.features.dev.navigation.DevProvider
import app.what.schedule.features.dev.navigation.devRegistry
import app.what.schedule.features.main.domain.MainController
import app.what.schedule.features.main.domain.models.MainEvent
import app.what.schedule.features.main.navigation.MainProvider
import app.what.schedule.features.schedule.navigation.ScheduleProvider
import app.what.schedule.features.schedule.navigation.scheduleRegistry
import app.what.schedule.features.settings.navigation.SettingsProvider
import app.what.schedule.features.settings.navigation.settingsRegistry
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Code
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainFeature(
    override val data: MainProvider
) : Feature<MainController, MainEvent>(),
    NavComponent<MainProvider>,
    KoinComponent {

    override val controller: MainController by inject()

    private companion object {
        val children = listOf(
            navItem("Расписание", Icons.Default.DateRange, ScheduleProvider),
            navItem("Настройки", Icons.Default.Settings, SettingsProvider)
        )

        val childrenRegistry: Registry = {
            settingsRegistry()
            scheduleRegistry()
            devRegistry()
        }
    }

    @Composable
    override fun content(modifier: Modifier) {
        val navigator = rememberHostNavigator()

        Box(
            Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .animatedStarsBackground()
        ) {
            NavigationHost(
                navigator = navigator,
                modifier = modifier.systemBarsPadding(),
                start = ScheduleProvider,
                registry = childrenRegistry
            )

            BottomNavBar(
                modifier = Modifier.align(Alignment.Companion.BottomCenter),
                navigator = navigator,
                screens = children
            ) {
                NavAction("Для разработчиков", WHATIcons.Code) {
                    navigator.c.navigate(DevProvider)
                }
            }

        }
    }
}
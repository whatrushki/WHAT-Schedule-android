package app.what.schedule.features.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import animatedStarsBackground
import app.what.foundation.core.Feature
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.ui.animations.AnimatedEnter
import app.what.foundation.ui.applyIf
import app.what.navigation.core.NavComponent
import app.what.navigation.core.NavProvider
import app.what.navigation.core.NavigationHost
import app.what.navigation.core.Registry
import app.what.navigation.core.bottom_navigation.BottomNavBar
import app.what.navigation.core.bottom_navigation.NavAction
import app.what.navigation.core.bottom_navigation.NavItem
import app.what.navigation.core.bottom_navigation.navItem
import app.what.navigation.core.rememberHostNavigator
import app.what.schedule.data.local.settings.rememberAppValues
import app.what.schedule.features.dev.navigation.DevProvider
import app.what.schedule.features.dev.navigation.devRegistry
import app.what.schedule.features.main.domain.MainController
import app.what.schedule.features.main.domain.models.MainEvent
import app.what.schedule.features.main.navigation.MainProvider
import app.what.schedule.features.news.navigation.NewsProvider
import app.what.schedule.features.news.navigation.newsRegistry
import app.what.schedule.features.schedule.navigation.ScheduleProvider
import app.what.schedule.features.schedule.navigation.scheduleRegistry
import app.what.schedule.features.settings.navigation.SettingsProvider
import app.what.schedule.features.settings.navigation.settingsRegistry
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.FrameBug
import app.what.schedule.ui.theme.icons.filled.News
import app.what.schedule.ui.theme.icons.filled.Person
import app.what.schedule.utils.Analytics
import app.what.schedule.utils.LogCat
import app.what.schedule.utils.LogScope
import app.what.schedule.utils.buildTag
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Serializable
object AccountProvider : NavProvider()

class MainFeature(
    override val data: MainProvider
) : Feature<MainController, MainEvent>(),
    NavComponent<MainProvider>,
    KoinComponent {
    
    override val controller: MainController by inject()
    
    val children: List<NavItem> = mutableListOf(
        navItem("Расписание", Icons.Default.DateRange, ScheduleProvider()),
        navItem("Настройки", Icons.Default.Settings, SettingsProvider)
    ).apply {
        add(
            0,
            if (controller.getState().hasProfilePage) navItem(
                "Профиль",
                WHATIcons.Person,
                AccountProvider
            )
            else navItem("Новости", WHATIcons.News, NewsProvider),
        )
    }
    
    val childrenRegistry: Registry = {
        settingsRegistry()
        newsRegistry()
        scheduleRegistry()
        devRegistry()
        composable<AccountProvider> { controller.getState().ui?.content(Modifier) }
    }
    
    @Composable
    override fun content(modifier: Modifier) {
        val navigator = rememberHostNavigator()
        val appValues = rememberAppValues()
        val useAnimation by appValues.useAnimation.collect()
        val devFeaturesEnabled by appValues.devPanelEnabled.collect()
//        var showBottomNavBar by useState(true)
        
        LaunchedEffect(Unit) {
            navigator.c.addOnDestinationChangedListener { _, destination, _ ->
                val navTag = buildTag(LogScope.CORE, LogCat.NAV)
                Auditor.debug(navTag, "Навигация: ${destination.route}")
                Analytics.logScreenView(destination.route ?: "no route")
                FirebaseCrashlytics.getInstance()
                    .setCustomKey("current_screen", destination.route ?: "unknown")
            }
        }
        
        Box(
            Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .applyIf(useAnimation == true) { animatedStarsBackground() }
        ) {
            NavigationHost(
                navigator = navigator,
                start = ScheduleProvider(),
                registry = childrenRegistry
            )
            
            AnimatedEnter(
//                showBottomNavBar,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                BottomNavBar(
                    navigator = navigator,
                    screens = children,
                ) {
                    if (!devFeaturesEnabled!!) null
                    else NavAction("Для разработчиков", WHATIcons.FrameBug) {
                        Analytics.logDevPanelOpen()
                        navigator.c.navigate(DevProvider)
                    }
                }
            }
        }
    }
}
package app.what.schedule.features.settings.navigation

import app.what.navigation.core.NavProvider
import app.what.navigation.core.Registry
import app.what.navigation.core.register
import app.what.schedule.features.settings.presentation.SettingsFeature
import kotlinx.serialization.Serializable

@Serializable
object SettingsProvider : NavProvider()

val settingsRegistry: Registry = {
    register(SettingsFeature::class)
}


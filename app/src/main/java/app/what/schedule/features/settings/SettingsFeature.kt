package app.what.schedule.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.what.foundation.core.Feature
import app.what.navigation.core.NavComponent
import app.what.schedule.features.settings.domain.SettingsController
import app.what.schedule.features.settings.domain.models.SettingsEvent
import app.what.schedule.features.settings.navigation.SettingsProvider
import app.what.schedule.features.settings.presentation.SettingsView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsFeature(
    override val data: SettingsProvider
) : Feature<SettingsController, SettingsEvent>(),
    NavComponent<SettingsProvider>,
    KoinComponent {

    override val controller: SettingsController by inject()

    @Composable
    override fun content(modifier: Modifier) {
        val state by controller.collectStates()

        SettingsView(state, listener)
    }
}
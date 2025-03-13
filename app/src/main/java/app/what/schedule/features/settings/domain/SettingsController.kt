package app.what.schedule.features.settings.domain

import app.what.foundation.core.UIController
import app.what.schedule.data.local.settings.AppSettingsRepository
import app.what.schedule.features.settings.domain.models.SettingsAction
import app.what.schedule.features.settings.domain.models.SettingsEvent
import app.what.schedule.features.settings.domain.models.SettingsState
import app.what.schedule.utils.AppUtils

class SettingsController(
    private val settings: AppSettingsRepository,
    private val appUtils: AppUtils
) : UIController<SettingsState, SettingsAction, SettingsEvent>(
    SettingsState()
) {
    override fun obtainEvent(viewEvent: SettingsEvent) = when (viewEvent) {
        is SettingsEvent.OnAppServerSelected -> onAppServerSelected(viewEvent)
        SettingsEvent.OnAppRestartClicked -> oAppRestartClicked()
    }

    private fun oAppRestartClicked() {
        updateState { copy(showRestartMessage = false) }
        appUtils.restart()
    }

    private fun onAppServerSelected(viewEvent: SettingsEvent.OnAppServerSelected) {
        settings.setUsedServer(viewEvent.value)
        updateState { copy(showRestartMessage = true) }
    }
}
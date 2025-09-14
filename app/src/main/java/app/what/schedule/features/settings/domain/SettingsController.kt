package app.what.schedule.features.settings.domain

import app.what.foundation.core.UIController
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.features.settings.domain.models.SettingsAction
import app.what.schedule.features.settings.domain.models.SettingsEvent
import app.what.schedule.features.settings.domain.models.SettingsState
import app.what.schedule.utils.AppUtils

class SettingsController(
    private val settings: AppValues,
    private val appUtils: AppUtils
) : UIController<SettingsState, SettingsAction, SettingsEvent>(
    SettingsState()
) {
    override fun obtainEvent(viewEvent: SettingsEvent) {}
}
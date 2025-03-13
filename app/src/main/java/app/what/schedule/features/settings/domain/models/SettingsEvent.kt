package app.what.schedule.features.settings.domain.models

import app.what.schedule.data.local.settings.AppSettingsRepository

sealed interface SettingsEvent {
    class OnAppServerSelected(val value: AppSettingsRepository.AppServers) : SettingsEvent
    object OnAppRestartClicked : SettingsEvent
}
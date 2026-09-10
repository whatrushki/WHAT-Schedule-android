package app.what.features.settings.di

import app.what.schedule.features.settings.domain.SettingsController
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val settingsFeatureModule = module {
    singleOf(::SettingsController)
}

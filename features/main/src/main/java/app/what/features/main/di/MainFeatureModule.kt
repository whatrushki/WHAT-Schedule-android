package app.what.features.main.di

import app.what.features.account.di.accountFeatureModule
import app.what.features.dev.di.devFeatureModule
import app.what.features.news.di.newsFeatureModule
import app.what.features.onboarding.di.onboardingFeatureModule
import app.what.features.schedule.di.scheduleFeatureModule
import app.what.features.settings.di.settingsFeatureModule
import app.what.schedule.features.main.domain.MainController
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val mainFeatureModule = module {
    singleOf(::MainController)
    includes(
        scheduleFeatureModule,
        newsFeatureModule,
        settingsFeatureModule,
        onboardingFeatureModule,
        accountFeatureModule,
        devFeatureModule
    )
}

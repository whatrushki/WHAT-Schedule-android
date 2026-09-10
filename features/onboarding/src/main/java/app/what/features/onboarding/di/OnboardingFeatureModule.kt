package app.what.features.onboarding.di

import app.what.schedule.features.onboarding.domain.OnboardingController
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val onboardingFeatureModule = module {
    singleOf(::OnboardingController)
}

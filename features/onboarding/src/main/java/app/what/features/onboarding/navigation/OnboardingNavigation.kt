package app.what.schedule.features.onboarding.navigation

import app.what.navigation.core.NavProvider
import app.what.navigation.core.Registry
import app.what.navigation.core.register
import app.what.schedule.features.onboarding.OnboardingFeature
import kotlinx.serialization.Serializable

@Serializable
object OnboardingProvider : NavProvider()

val onboardingRegistry: Registry = {
    register(OnboardingFeature::class)
}


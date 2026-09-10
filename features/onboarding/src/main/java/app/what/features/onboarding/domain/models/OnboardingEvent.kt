package app.what.schedule.features.onboarding.domain.models

sealed interface OnboardingEvent {
    object Init : OnboardingEvent
    data class SelectInstitution(val id: String) : OnboardingEvent
    object FinishOnboarding : OnboardingEvent
}
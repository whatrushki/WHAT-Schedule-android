package app.what.schedule.features.onboarding.domain.models

sealed interface OnboardingAction {
    object NavigateToMain : OnboardingAction
}
package app.what.schedule.features.onboarding.domain.models

import app.what.schedule.data.remote.api.Institution
import app.what.schedule.data.remote.api.InstitutionFilial
import app.what.schedule.data.remote.api.InstitutionProvider

sealed interface OnboardingEvent {
    object Init : OnboardingEvent
    class InstitutionAndProviderSelected(
        val institution: Institution,
        val filial: InstitutionFilial,
        val provider: InstitutionProvider.Factory
    ) : OnboardingEvent
}
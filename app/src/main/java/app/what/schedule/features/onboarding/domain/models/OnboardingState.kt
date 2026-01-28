package app.what.schedule.features.onboarding.domain.models

import app.what.schedule.data.remote.api.Institution

data class OnboardingState(
    val institutions: List<Institution.Factory> = emptyList(),
    val selectedInstitutionId: String? = null,
) {
    val canFinish: Boolean get() = selectedInstitutionId != null
}


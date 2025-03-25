package app.what.schedule.features.onboarding.domain

import app.what.foundation.core.UIController
import app.what.schedule.data.local.settings.AppSettingsRepository
import app.what.schedule.data.remote.api.InstitutionManager
import app.what.schedule.features.onboarding.domain.models.OnboardingAction
import app.what.schedule.features.onboarding.domain.models.OnboardingEvent
import app.what.schedule.features.onboarding.domain.models.OnboardingState


class OnboardingController(
    private val institutionManager: InstitutionManager,
    private val settings: AppSettingsRepository
) : UIController<OnboardingState, OnboardingAction, OnboardingEvent>(
    OnboardingState()
) {
    init {
        updateState { copy(institutions = institutionManager.getInstitutions()) }
    }

    override fun obtainEvent(viewEvent: OnboardingEvent) = when (viewEvent) {
        OnboardingEvent.Init -> {}
        is OnboardingEvent.InstitutionAndProviderSelected ->
            institutionAndProviderSelected(viewEvent)
    }

    private fun institutionAndProviderSelected(
        viewEvent: OnboardingEvent.InstitutionAndProviderSelected
    ) {
        institutionManager.save(
            viewEvent.institution.metadata.id,
            viewEvent.filial.metadata.id,
            viewEvent.provider.metadata.id
        )

        finishAndGoToMain()
    }

    private fun finishAndGoToMain() {
        settings.setFirstLaunch(false)
        setAction(OnboardingAction.NavigateToMain)
    }
}
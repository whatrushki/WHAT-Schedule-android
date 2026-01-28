package app.what.schedule.features.onboarding.domain

import app.what.foundation.core.UIController
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.remote.api.InstitutionManager
import app.what.schedule.features.onboarding.domain.models.OnboardingAction
import app.what.schedule.features.onboarding.domain.models.OnboardingEvent
import app.what.schedule.features.onboarding.domain.models.OnboardingState


class OnboardingController(
    private val institutionManager: InstitutionManager,
    private val settings: AppValues
) : UIController<OnboardingState, OnboardingAction, OnboardingEvent>(
    OnboardingState()
) {
    init {
        updateState { copy(institutions = institutionManager.getInstitutions()) }
    }

    override fun obtainEvent(viewEvent: OnboardingEvent) = when (viewEvent) {
        OnboardingEvent.Init -> {}

        is OnboardingEvent.SelectInstitution -> updateState {
            copy(selectedInstitutionId = viewEvent.id)
        }

        OnboardingEvent.FinishOnboarding -> finishAndGoToMain()
    }

    private fun finishAndGoToMain() {
        val selectedInstId = viewState.selectedInstitutionId ?: return

        institutionManager.save(selectedInstId)

        settings.isFirstLaunch.set(false)
        setAction(OnboardingAction.NavigateToMain)
    }
}
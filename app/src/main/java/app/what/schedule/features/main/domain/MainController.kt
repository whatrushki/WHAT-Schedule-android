package app.what.schedule.features.main.domain

import app.what.foundation.core.UIController
import app.what.schedule.data.remote.api.InstitutionManager
import app.what.schedule.features.main.domain.models.MainAction
import app.what.schedule.features.main.domain.models.MainEvent
import app.what.schedule.features.main.domain.models.MainState

class MainController(
    val institutionManager: InstitutionManager
) : UIController<MainState, MainAction, MainEvent>(
    MainState()
) {
    init {
        val accountService = institutionManager.getSavedInstitution()?.accountFeature
        updateState { copy(hasProfilePage = accountService != null, ui = accountService) }
    }

    override fun obtainEvent(viewEvent: MainEvent) = when (viewEvent) {
        else -> {}
    }
}
package app.what.schedule.features.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.what.foundation.core.Feature
import app.what.navigation.core.NavComponent
import app.what.navigation.core.rememberNavigator
import app.what.schedule.features.main.navigation.MainProvider
import app.what.schedule.features.onboarding.domain.OnboardingController
import app.what.schedule.features.onboarding.domain.models.OnboardingAction
import app.what.schedule.features.onboarding.domain.models.OnboardingEvent
import app.what.schedule.features.onboarding.navigation.OnboardingProvider
import app.what.schedule.features.onboarding.presentation.OnboardingView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OnboardingFeature(
    override val data: OnboardingProvider
) : Feature<OnboardingController, OnboardingEvent>(),
    NavComponent<OnboardingProvider>,
    KoinComponent {
    override val controller: OnboardingController by inject()

    @Composable
    override fun content(modifier: Modifier) = Column(
        modifier.fillMaxSize()
    ) {
        val viewState by controller.collectStates()
        val viewAction by controller.collectActions()
        val navigator = rememberNavigator()

        LaunchedEffect(Unit) {
            listener(OnboardingEvent.Init)
        }

        OnboardingView(viewState, listener)

        when (viewAction) {
            OnboardingAction.NavigateToMain -> {
                navigator.c.navigate(MainProvider) {
                    popUpTo(OnboardingProvider) {
                        inclusive = true
                    }
                }

                controller.clearAction()
            }

            null -> Unit
        }
    }
}
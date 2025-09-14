package app.what.schedule.features.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.what.foundation.core.Feature
import app.what.navigation.core.NavComponent
import app.what.schedule.features.schedule.domain.ScheduleController
import app.what.schedule.features.schedule.domain.models.ScheduleEvent
import app.what.schedule.features.schedule.navigation.ScheduleProvider
import app.what.schedule.features.schedule.presentation.ScheduleView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ScheduleFeature(
    override val data: ScheduleProvider
) : Feature<ScheduleController, ScheduleEvent>(),
    NavComponent<ScheduleProvider>,
    KoinComponent {
    override val controller: ScheduleController by inject()

    @Composable
    override fun content(modifier: Modifier) = Column(
        modifier.fillMaxSize()
    ) {
        val viewState by controller.collectStates()

        LaunchedEffect(Unit) {
            listener(ScheduleEvent.Init)
        }

        ScheduleView(viewState, listener)
    }
}
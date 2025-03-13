package app.what.schedule.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.what.foundation.core.Listener
import app.what.foundation.data.rememberEnumPreference
import app.what.foundation.ui.Gap
import app.what.foundation.ui.SegmentTab
import app.what.schedule.data.local.settings.AppSettingsRepository
import app.what.schedule.features.settings.domain.models.SettingsEvent
import app.what.schedule.features.settings.domain.models.SettingsState
import app.what.schedule.presentation.theme.icons.WHATIcons
import app.what.schedule.presentation.theme.icons.filled.Domain
import app.what.schedule.presentation.theme.icons.filled.MeetingRoom

@Composable
fun SettingsView(
    state: SettingsState,
    listener: Listener<SettingsEvent>
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.fillMaxSize()
) {
    val appServer = rememberEnumPreference(
        AppSettingsRepository.Keys.USED_SERVER,
        AppSettingsRepository.AppServers.RKSI
    )

    SingleChoiceSegmentedButtonRow(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        AppSettingsRepository.AppServers.entries.forEach {
            SegmentTab(
                it.ordinal,
                AppSettingsRepository.AppServers.entries.size,
                appServer == it,
                icon = when (it) {
                    AppSettingsRepository.AppServers.RKSI -> WHATIcons.Domain
                    AppSettingsRepository.AppServers.TURTLE -> WHATIcons.MeetingRoom
                },
                label = it.name
            ) {
                listener(SettingsEvent.OnAppServerSelected(it))
            }
        }
    }

    if (state.showRestartMessage) Box(
        Modifier.fillMaxWidth().padding(20.dp, 0.dp, 20.dp, 20.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp)
        ) {
            Text(
                "Требуется перезапуск",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )

            Gap(8)

            Button(
                onClick = {
                    listener(SettingsEvent.OnAppRestartClicked)
                }
            ) {
                Text("Перезапустить")
            }
        }
    }
}
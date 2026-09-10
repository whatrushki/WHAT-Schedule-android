package app.what.schedule.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.what.domain.services.UpdateInfo
import app.what.schedule.desktop.model.UniType

@Composable
fun DesktopHeaderBar(
    selectedUni: UniType,
    onSelectUni: (UniType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "WHAT Schedule Desktop",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp)
            )
            UniType.entries.forEach { uni ->
                FilterChip(
                    selected = selectedUni == uni,
                    onClick = { onSelectUni(uni) },
                    label = { Text(uni.title) }
                )
            }
        }
    }
}

@Composable
fun UpdateBanner(
    updateInfo: UpdateInfo?,
    onUpdateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (updateInfo == null) return

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Доступна новая версия: ${updateInfo.version}",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = onUpdateClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Обновить")
            }
        }
    }
}

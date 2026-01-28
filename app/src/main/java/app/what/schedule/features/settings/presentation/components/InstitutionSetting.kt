package app.what.schedule.features.settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.data.settings.PreferenceStorage
import app.what.foundation.data.settings.types.asDialog
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.bclick
import app.what.foundation.ui.controllers.rememberDialogController
import app.what.schedule.data.remote.api.insts

fun PreferenceStorage.Value<String>.asInstitutionChoice(
    sideEffect: (String?) -> Unit
) = asDialog { value, set ->
    val dialog = rememberDialogController()
    val selected by value.collect()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp) // Отступ от списка
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFD54F).copy(alpha = 0.15f))
                .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icons.Default.Warning.Show(
                color = Color(0xFFFFA000),
                modifier = Modifier.size(20.dp)
            )

            Gap(12)

            Text(
                text = "Приложение будет перезагружено",
                style = typography.bodySmall,
                color = colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 16.sp
            )
        }

        insts.forEach {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .bclick {

                        set(it.metadata.id)
                        dialog.close()
                        sideEffect(it.metadata.id)
                    }
                    .padding(12.dp, 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selected!! == it.metadata.id, onClick = null)
                Gap(12)
                Text(it.metadata.name)
            }
        }
    }
}
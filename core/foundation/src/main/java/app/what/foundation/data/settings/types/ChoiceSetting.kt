package app.what.foundation.data.settings.types

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.what.foundation.core.UIComponent
import app.what.foundation.data.settings.PreferenceStorage
import app.what.foundation.data.settings.views.BaseSettingRow
import app.what.foundation.ui.Gap
import app.what.foundation.ui.bclick
import app.what.foundation.ui.controllers.rememberDialogController

fun <T : Enum<T>> PreferenceStorage.Value<T>.asSingleChoice(
    options: Array<T>,
    onDisplay: (T) -> String = { it.toString() }
) = object : UIComponent {
    @Composable
    override fun content(modifier: Modifier) {
        val state by collect()
        val dialog = rememberDialogController()

        BaseSettingRow(
            value = this@asSingleChoice,
            modifier = modifier,
            onClick = {
                dialog.open {
                    ChoiceDialogContent(
                        options = options,
                        selected = state,
                        onDisplay = onDisplay,
                        onSelect = { set(it); dialog.close() }
                    )
                }
            }
        ) {
            Text(state?.let(onDisplay) ?: "Не выбрано", color = colorScheme.primary)
        }
    }
}

@Composable
fun <T> ChoiceDialogContent(
    options: Array<T>,
    selected: T?,
    onDisplay: (T) -> String,
    onSelect: (T) -> Unit
) = Column(
    modifier = Modifier.fillMaxWidth().padding(8.dp),
) {
    options.forEach { option ->
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .bclick { onSelect(option) }
                .padding(12.dp, 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = option == selected, onClick = null)
            Gap(12)
            Text(onDisplay(option))
        }
    }
}
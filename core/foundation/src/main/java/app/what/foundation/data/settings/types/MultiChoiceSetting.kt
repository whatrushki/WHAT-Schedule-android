package app.what.foundation.data.settings.types

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.what.foundation.core.UIComponent
import app.what.foundation.data.settings.PreferenceStorage
import app.what.foundation.data.settings.views.BaseSettingRow
import app.what.foundation.ui.Gap
import app.what.foundation.ui.bclick
import app.what.foundation.ui.controllers.rememberDialogController

fun <T : Any> PreferenceStorage.Value<Set<T>>.asMultiChoice(
    options: Array<T>,
    onDisplay: (T) -> String = { it.toString() }
) = object : UIComponent {
    @Composable
    override fun content(modifier: Modifier) {
        val state by collect { it ?: emptySet() }
        val dialog = rememberDialogController()

        BaseSettingRow(
            value = this@asMultiChoice,
            modifier = modifier,
            onClick = { dialog.open {
                MultiChoiceDialogContent(
                    options = options,
                    selected = state!!,
                    onDisplay = onDisplay,
                    onToggle = { item ->
                        val next = if (state!!.contains(item)) state!! - item else state!! + item
                        set(next)
                    }
                )
            } }
        ) {
            Text("${state!!.size} выбрано", color = colorScheme.primary)
        }
    }
}

@Composable
private fun <T> MultiChoiceDialogContent(
    options: Array<T>,
    selected: Set<T>,
    onDisplay: (T) -> String,
    onToggle: (T) -> Unit
) = Column(Modifier.fillMaxWidth().padding(16.dp)) {
    options.forEach { option ->
        Row(
            Modifier.fillMaxWidth().bclick { onToggle(option) }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = selected.contains(option), onCheckedChange = null)
            Gap(12)
            Text(onDisplay(option))
        }
    }
}
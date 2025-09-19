package app.what.foundation.data.settings.supporting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.what.foundation.data.settings.types.Named
import app.what.foundation.ui.Gap
import app.what.foundation.ui.bclick

@Composable
fun <T> EnumSelection(
    currentValue: T?,
    options: Array<T>,
    onSelectionChange: (T) -> Unit,
    onDismiss: () -> Unit
) where T : Enum<T>, T : Named = Column {
    Text(
        text = "Выберите вариант",
        style = typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp)
    )

    options.forEach { option ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .bclick { onSelectionChange(option) }
                .padding(8.dp, 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = currentValue == option,
                onClick = { onSelectionChange(option) }
            )

            Gap(16)

            Text(
                text = option.displayName,
                style = typography.bodyLarge
            )
        }
    }
}
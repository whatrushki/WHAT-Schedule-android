package app.what.foundation.data.settings.supporting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.what.foundation.ui.Gap

@Composable
internal fun StringEditWithValidation(
    title: String,
    currentValue: String,
    hint: String,
    error: String?,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    maxLength: Int? = null
) = Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    Text(
        text = title,
        style = typography.headlineSmall,
        color = colorScheme.primary
    )

    Column {
        OutlinedTextField(
            value = currentValue,
            onValueChange = { newValue ->
                if (maxLength == null || newValue.length <= maxLength) {
                    onValueChange(newValue)
                }
            },
            label = { Text(hint) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = error != null,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (error == null) onSave() }
            )
        )

        if (error != null) {
            Text(
                text = error,
                color = colorScheme.error,
                style = typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        maxLength?.let {
            Text(
                text = "${currentValue.length}/$maxLength",
                color = if (currentValue.length > maxLength) colorScheme.error
                else colorScheme.secondary,
                style = typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onDismiss) {
            Text("Отмена")
        }
        Gap(8)
        Button(
            onClick = onSave,
            enabled = error == null
        ) {
            Text("Сохранить")
        }
    }
}
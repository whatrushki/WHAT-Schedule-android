package app.what.foundation.data.settings.types

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.what.foundation.core.UIComponent
import app.what.foundation.data.settings.PreferenceStorage
import app.what.foundation.data.settings.views.BaseSettingRow
import app.what.foundation.ui.Gap
import app.what.foundation.ui.controllers.rememberDialogController
import app.what.foundation.ui.useState
import kotlinx.coroutines.delay

fun PreferenceStorage.Value<Int>.asIntInput(
    placeholder: String = "0"
) = object : UIComponent {
    @Composable
    override fun content(modifier: Modifier) {
        val savedValue by collect()
        val dialog = rememberDialogController()

        BaseSettingRow(this@asIntInput, modifier, onClick = {
            dialog.open {
                var localText by useState(savedValue?.toString() ?: "")

                InputDialogLayout(
                    title = title,
                    value = localText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) localText = it },
                    placeholder = placeholder,
                    keyboardType = KeyboardType.Number,
                    onSave = {
                        set(localText.toIntOrNull() ?: 0)
                        dialog.close()
                    }
                )
            }
        }) {
            Text(
                savedValue?.toString() ?: "0",
                color = colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun PreferenceStorage.Value<String>.asStringInput(
    debounceMs: Long = 600L,
    placeholder: String = "Введите текст..."
) = object : UIComponent {
    @Composable
    override fun content(modifier: Modifier) {
        val savedValue by collect()
        val dialog = rememberDialogController()

        BaseSettingRow(this@asStringInput, modifier, onClick = {
            dialog.open {
                var localText by useState(savedValue ?: "")

                LaunchedEffect(localText) {
                    if (localText == savedValue) return@LaunchedEffect
                    delay(debounceMs)
                    set(localText)
                }

                InputDialogLayout(
                    title = title,
                    value = localText,
                    onValueChange = { localText = it },
                    placeholder = placeholder,
                    keyboardType = KeyboardType.Text
                )
            }
        }) {
            Text(savedValue?.take(15)?.let { if (it.length < 15) it else "$it..." } ?: "Пусто",
                color = colorScheme.primary)
        }
    }
}

@Composable
private fun InputDialogLayout(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    onSave: (() -> Unit)? = null
) {
    Column(
        Modifier
            .padding(24.dp)
            .fillMaxWidth()
    ) {
        Text(title, style = typography.titleLarge, color = colorScheme.primary)
        Gap(16)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = shapes.medium
        )

        if (onSave != null) {
            Gap(24)
            Button(
                onClick = onSave,
                modifier = Modifier.align(Alignment.End),
                shape = shapes.small
            ) { Text("Сохранить") }
        } else {
            Gap(8)
            Text(
                "Сохраняется автоматически...",
                style = typography.labelSmall,
                color = colorScheme.secondary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
package app.what.schedule.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import app.what.foundation.ui.bclick
import app.what.foundation.ui.useState
import kotlinx.coroutines.delay

@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    debounce: Long = 0,
    placeholder: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val (text, setText) = useState(value)
    var enabled by useState(false)

    LaunchedEffect(text) {
        delay(debounce)
        if (text != value) onValueChange(text)
    }

    OutlinedTextField(
        enabled = enabled,
        modifier = modifier
            .clip(CircleShape)
            .bclick { enabled = true },
        value = text,
        onValueChange = setText,
        placeholder = placeholder?.let {
            {
                Text(
                    it,
                    style = typography.bodySmall
                )
            }
        },
        leadingIcon = leading,
        trailingIcon = trailing,
        singleLine = true,
        shape = CircleShape,
        textStyle = typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colorScheme.surfaceContainer,
            unfocusedContainerColor = colorScheme.surfaceContainer,
            focusedIndicatorColor = colorScheme.primary,
            unfocusedIndicatorColor = colorScheme.outlineVariant
        ),
    )
}
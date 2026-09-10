package app.what.schedule.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Fallback(
    text: String,
    modifier: Modifier = Modifier,
    action: Pair<String, () -> Unit>? = null,
    showImage: Boolean = true
) = app.what.foundation.ui.components.Fallback(
    text = text,
    modifier = modifier,
    action = action,
    showImage = showImage
)

@Composable
fun AsyncImageWithFallback(
    url: String?,
    modifier: Modifier = Modifier,
    headers: Map<String, String> = emptyMap(),
    enableDetailView: Boolean = false
) = app.what.foundation.ui.components.AsyncImageWithFallback(
    url = url,
    modifier = modifier,
    headers = headers,
    enableDetailView = enableDetailView
)

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    debounce: Long = 0,
    placeholder: String? = null,
    shape: androidx.compose.ui.graphics.Shape = androidx.compose.foundation.shape.CircleShape,
    options: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    disabled: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) = app.what.foundation.ui.components.StyledTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    debounce = debounce,
    placeholder = placeholder,
    shape = shape,
    options = options,
    disabled = disabled,
    leading = leading,
    trailing = trailing
)

@Composable
fun PolicyView() = app.what.foundation.ui.components.PolicyView()



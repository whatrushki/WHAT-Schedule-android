package app.what.schedule.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.what.foundation.ui.components.Fallback as FoundationFallback

@Composable
fun Fallback(
    text: String,
    modifier: Modifier = Modifier,
    action: Pair<String, () -> Unit>? = null,
    showImage: Boolean = true
) {
    FoundationFallback(
        text = text,
        modifier = modifier,
        action = action,
        showImage = showImage
    )
}

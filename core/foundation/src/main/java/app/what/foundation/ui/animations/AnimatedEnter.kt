package app.what.foundation.ui.animations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun AnimatedEnter(
    modifier: Modifier = Modifier,
    delay: Long = 0,
    duration: Int = 500,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (delay > 0) delay(delay)
        visible = true
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(tween(duration)) + slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(duration)
        )
    ) {
        content()
    }
}
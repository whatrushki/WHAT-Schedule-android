package app.what.foundation.ui.animations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
    val visibleState = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        if (delay > 0) delay(delay)
        visibleState.targetState = true
    }

    AnimatedEnter(
        visible = visibleState.targetState,
        modifier = modifier,
        duration = duration,
        content = content
    )
}

@Composable
fun AnimatedEnter(
    visible: Boolean,
    modifier: Modifier = Modifier,
    duration: Int = 500,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(duration, easing = FastOutSlowInEasing)
        ) + slideInVertically(
            animationSpec = tween(duration, easing = FastOutSlowInEasing),
            initialOffsetY = { it / 2 }
        ),
        exit = fadeOut(tween(duration)) + slideOutVertically(
            targetOffsetY = { it / 2 }
        )
    ) { content() }
}
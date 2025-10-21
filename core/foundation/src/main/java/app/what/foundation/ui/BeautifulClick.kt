package app.what.foundation.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed


fun Modifier.bclick(enabled: Boolean = true, block: () -> Unit): Modifier = composed {
    clickable(
        indication = LocalIndication.current,
        interactionSource = remember { MutableInteractionSource() },
        enabled = enabled,
        onClick = block
    )
}
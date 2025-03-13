package app.what.foundation.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity

@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)
}

@Composable
fun OnKeyboardHiddenListener(onKeyboardHidden: () -> Unit) {
    val keyboardVisible by keyboardAsState()

    LaunchedEffect(keyboardVisible) {
        if (!keyboardVisible) {
            onKeyboardHidden()
        }
    }
}
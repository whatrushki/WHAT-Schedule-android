package app.what.foundation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.capplyIf(
    expression: Boolean,
    elseBlock: @Composable Modifier.() -> Modifier = { this },
    block: @Composable Modifier.() -> Modifier
): Modifier = composed { if (expression) block() else elseBlock() }

fun Modifier.applyIf(
    expression: Boolean,
    elseBlock: Modifier.() -> Modifier = { this },
    block: Modifier.() -> Modifier
) = if (expression) block() else elseBlock()
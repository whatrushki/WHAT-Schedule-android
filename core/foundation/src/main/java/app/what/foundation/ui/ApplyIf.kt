package app.what.foundation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun Modifier.applyComposableIf(
    expression: Boolean,
    elseBlock: @Composable Modifier.() -> Modifier = { this },
    block: @Composable Modifier.() -> Modifier
): Modifier {
    val exp by remember { derivedStateOf { expression } }
    return if (exp) block() else elseBlock()
}

fun Modifier.applyIf(
    expression: Boolean,
    elseBlock: Modifier.() -> Modifier = { this },
    block: Modifier.() -> Modifier
) = if (expression) block() else elseBlock()
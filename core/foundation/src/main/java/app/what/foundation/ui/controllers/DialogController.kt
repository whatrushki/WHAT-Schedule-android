package app.what.foundation.ui.controllers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope


@Composable
fun rememberDialogController(): DialogController = LocalDialogController.current

@Composable
fun rememberDialogHostController(
    start: @Composable () -> Unit = {},
    scope: CoroutineScope = rememberCoroutineScope()
): DialogController {
    return remember {
        object : DialogController {
            override var full by mutableStateOf(false)
            override var content by mutableStateOf(start)
            override var cancellable by mutableStateOf(true)
            override var opened by mutableStateOf(false)

            override fun open(
                full: Boolean,
                cancellable: Boolean,
                content: @Composable () -> Unit
            ) {
                this.full = full
                this.content = content
                this.cancellable = cancellable
                opened = true
            }

            override fun close() {
                opened = false
            }
        }
    }
}

val LocalDialogController = staticCompositionLocalOf<DialogController> {
    error("DialogController не предоставлен")
}

interface DialogController {
    var full: Boolean
    var opened: Boolean
    var cancellable: Boolean
    var content: @Composable () -> Unit

    fun open(
        full: Boolean = false,
        cancellable: Boolean = true,
        content: @Composable () -> Unit
    )

    fun close()
}
package app.what.foundation.ui.controllers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import app.what.foundation.core.Monitor.Companion.monitored
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
            override var content by monitored(start)
            override var cancellable by monitored(true)
            override var opened by monitored(false)

            override fun open(
                cancellable: Boolean,
                content: @Composable () -> Unit
            ) {
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
    var opened: Boolean
    var cancellable: Boolean
    var content: @Composable () -> Unit

    fun open(
        cancellable: Boolean = true,
        content: @Composable () -> Unit
    )

    fun close()
}
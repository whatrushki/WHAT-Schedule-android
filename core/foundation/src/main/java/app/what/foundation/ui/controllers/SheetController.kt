package app.what.foundation.ui.controllers

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import app.what.foundation.utils.retry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun rememberSheetController(): SheetController = LocalSheetController.current

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSheetHostController(
    start: @Composable () -> Unit = {},
    scope: CoroutineScope = rememberCoroutineScope()
): SheetController {
    var sheetState by remember { mutableStateOf<SheetState?>(null) }

    return remember {
        object : SheetController {
            override var content by mutableStateOf(start)
            override var cancellable by mutableStateOf(true)
            override var opened by mutableStateOf(false)

            override fun setSheetState(state: SheetState) {
                sheetState = state
            }

            override fun open(
                full: Boolean,
                cancellable: Boolean,
                content: @Composable () -> Unit
            ) {
                this.content = content
                this.cancellable = cancellable
                open(full)
            }

            override fun open(full: Boolean) {
                opened = true
                scope.launch {
                    delay(300)
                    // TODO: сумашедший костыль, исправить по возможности
                    retry(9, 100) {
                        if (full) sheetState?.expand()
                    }
                }
            }

            override fun close() {
                opened = false
            }

            override fun animateClose() {
                scope.launch { sheetState?.hide() }
                    .invokeOnCompletion { close() }
            }
        }
    }
}


val LocalSheetController = staticCompositionLocalOf<SheetController> { error("непон") }

interface SheetController {
    val opened: Boolean
    var cancellable: Boolean
    var content: @Composable () -> Unit

    fun open(full: Boolean = false)
    fun open(
        full: Boolean = false,
        cancellable: Boolean = true,
        content: @Composable () -> Unit
    )

    fun close()
    fun animateClose()

    @OptIn(ExperimentalMaterial3Api::class)
    fun setSheetState(state: SheetState)
}
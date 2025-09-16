package app.what.navigation.core

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.what.foundation.core.Monitor.Companion.monitored
import kotlinx.coroutines.CoroutineScope

@Composable
fun ProvideGlobalDialog(
    controller: DialogController = rememberDialogHostController(),
    transitionSpec: AnimatedContentTransitionScope<@Composable () -> Unit>.() -> ContentTransform = {
        fadeIn() togetherWith fadeOut()
    },
    content: @Composable () -> Unit
) = CompositionLocalProvider(
    LocalDialogController provides controller,
) {
    content()

    if (controller.opened) {
        Dialog(
            onDismissRequest = controller::close,
            properties = DialogProperties(
                dismissOnBackPress = controller.cancellable,
                dismissOnClickOutside = controller.cancellable
            )
        ) {
            Surface(
                shape = shapes.medium,
                color = colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                AnimatedContent(
                    targetState = controller.content,
                    transitionSpec = transitionSpec,
                    label = "AnimatedDialogContent"
                ) { dialogContent ->
                    dialogContent()
                }
            }
        }
    }
}

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


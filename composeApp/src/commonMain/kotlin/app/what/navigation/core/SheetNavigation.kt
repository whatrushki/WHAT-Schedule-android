package app.what.navigation.core

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.what.foundation.ui.controllers.LocalSheetController
import app.what.foundation.ui.controllers.SheetController
import app.what.foundation.ui.controllers.rememberSheetHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvideGlobalSheet(
    controller: SheetController = rememberSheetHostController(),
    content: @Composable () -> Unit
) = CompositionLocalProvider(
    LocalSheetController provides controller
) {
    val state = rememberModalBottomSheetState(
        confirmValueChange = {
            if (it != SheetValue.Hidden) true
            else controller.cancellable
        }
    )

    LaunchedEffect(Unit) {
        controller.setSheetState(state)
    }

    content()

    if (controller.opened) {
        ModalBottomSheet(
            onDismissRequest = { controller.close() },
            sheetState = state
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = controller.content,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "AnimatedSheetContent"
                ) { sheetContent ->
                    sheetContent()
                }
            }
        }
    }
}

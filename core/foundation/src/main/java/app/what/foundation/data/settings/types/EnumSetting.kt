package app.what.foundation.data.settings.types

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.what.foundation.data.settings.Setting
import app.what.foundation.data.settings.supporting.EnumSelection
import app.what.foundation.data.settings.views.BaseSettingView
import app.what.foundation.ui.controllers.rememberDialogController

interface Named {
    val displayName: String
}

@Composable
inline fun <reified T> EnumSettingView(
    modifier: Modifier,
    setting: Setting<T>,
) where T : Enum<T>, T : Named {
    val value by setting.value.collect()
    val dialogController = rememberDialogController()

    BaseSettingView(
        modifier = modifier,
        icon = setting.icon,
        title = setting.title,
        description = setting.description,
        supportingContent = {
            EnumSelection(
                currentValue = value,
                options = enumValues<T>(),
                onSelectionChange = { newValue ->
                    setting.value.set(newValue)
                    dialogController.close()
                },
                onDismiss = dialogController::close
            )
        }
    )
}
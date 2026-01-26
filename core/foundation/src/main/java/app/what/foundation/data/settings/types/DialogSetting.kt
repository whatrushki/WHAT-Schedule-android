package app.what.foundation.data.settings.types

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.what.foundation.core.UIComponent
import app.what.foundation.data.settings.PreferenceStorage
import app.what.foundation.data.settings.views.BaseSettingRow
import app.what.foundation.ui.controllers.rememberDialogController

fun <T : Any> PreferenceStorage.Value<T>.asDialog(
    onContent: @Composable (value: PreferenceStorage.Value<T>, setValue: (T?) -> Unit) -> Unit
) = object : UIComponent {
    @Composable
    override fun content(modifier: Modifier) {
        val dialog = rememberDialogController()

        BaseSettingRow(
            value = this@asDialog,
            modifier = modifier,
            onClick = { dialog.open { onContent(this@asDialog, this@asDialog::set) } }
        ) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = colorScheme.outline)
        }
    }
}
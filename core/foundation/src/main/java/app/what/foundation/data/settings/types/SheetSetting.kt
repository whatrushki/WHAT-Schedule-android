package app.what.foundation.data.settings.types

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.what.foundation.core.UIComponent
import app.what.foundation.data.settings.PreferenceStorage
import app.what.foundation.data.settings.views.BaseSettingRow
import app.what.foundation.ui.controllers.rememberSheetController

fun <T : Any> PreferenceStorage.Value<T>.asSheet(
    onContent: @Composable (value: PreferenceStorage.Value<T>, setValue: (T?) -> Unit) -> Unit
) = object : UIComponent {
    @Composable
    override fun content(modifier: Modifier) {
        val sheet = rememberSheetController()

        BaseSettingRow(
            value = this@asSheet,
            modifier = modifier,
            onClick = {
                sheet.open { onContent(this@asSheet, this@asSheet::set) }
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
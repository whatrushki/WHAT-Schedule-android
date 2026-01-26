package app.what.foundation.data.settings.types

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.what.foundation.core.UIComponent
import app.what.foundation.data.settings.PreferenceStorage
import app.what.foundation.data.settings.views.BaseSettingRow

fun PreferenceStorage.Value<Boolean>.asSwitch() = object : UIComponent {
    @Composable override fun content(modifier: Modifier) {
        val state by collect()
        BaseSettingRow(this@asSwitch, modifier) {
            Switch(checked = state ?: false, onCheckedChange = { set(it) })
        }
    }
}
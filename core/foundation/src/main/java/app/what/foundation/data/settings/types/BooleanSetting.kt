package app.what.foundation.data.settings.types

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.what.foundation.data.settings.Setting
import app.what.foundation.data.settings.views.BaseSettingView

val BooleanSetting = @Composable
fun(
    modifier: Modifier,
    setting: Setting<Boolean>
) {
    val value by setting.value.collect()
    val checked = value ?: false

    BaseSettingView(
        modifier = modifier,
        icon = setting.icon,
        title = setting.title,
        description = setting.description,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = setting.value::set
            )
        }
    )
}
package app.what.foundation.data.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import app.what.foundation.core.UIComponent

data class Setting<T : Any>(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val value: PreferenceStorage.Value<T>,
    val view: @Composable (Modifier, Setting<T>) -> Unit
) : UIComponent {
    @Composable
    override fun content(modifier: Modifier) = view(modifier, this)
}
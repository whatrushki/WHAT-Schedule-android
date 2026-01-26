package app.what.foundation.data.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.what.foundation.core.UIComponent
import app.what.foundation.services.AppLogger.Companion.Auditor

fun UIComponent.visibleIf(condition: @Composable () -> Boolean): UIComponent = object : UIComponent by this {
    @Composable
    override fun content(modifier: Modifier) {
        AnimatedVisibility(
            visible = condition(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) { this@visibleIf.content(modifier) }
    }
}

fun <T : Any> UIComponent.dependsOn(
    preference: PreferenceStorage.Value<T>,
    predicate: (T?) -> Boolean
): UIComponent = visibleIf {
    val state by preference.collect()
    predicate(state)
}
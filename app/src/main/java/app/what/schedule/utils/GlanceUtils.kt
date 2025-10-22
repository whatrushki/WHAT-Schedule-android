package app.what.schedule.utils

import android.content.Context
import android.content.res.Configuration

object GlanceUtils {
    fun Context.isSystemInDarkTheme(): Boolean {
        return when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }
}
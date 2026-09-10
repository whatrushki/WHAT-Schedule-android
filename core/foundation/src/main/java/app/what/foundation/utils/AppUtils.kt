package app.what.foundation.utils

import android.content.Context
import com.jakewharton.processphoenix.ProcessPhoenix


class AppUtils(private val context: Context) {
    fun restart() = ProcessPhoenix.triggerRebirth(context)
}
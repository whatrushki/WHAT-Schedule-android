package app.what.schedule.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class AppUtils(private val context: Context) {
    fun restart() {
        val packageManager: PackageManager = context.packageManager
        val intent: Intent = packageManager.getLaunchIntentForPackage(context.packageName)!!
        val componentName: ComponentName = intent.component!!
        val restartIntent: Intent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(restartIntent)
        Runtime.getRuntime().exit(0)
    }
}
package app.what.foundation.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.what.foundation.ui.Gap
import app.what.foundation.ui.useState
import androidx.core.net.toUri

@Composable
fun PermissionRequired(
    permission: String,
    permissionName: String,
    content: @Composable () -> Unit
) {
    var hasPermission by useState(false)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasPermission = it }
    )

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) content()
    else { //Check for Android 12+
        hasPermission = ContextCompat.checkSelfPermission(
            LocalContext.current, permission
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) content()
        else Button(
            onClick = { launcher.launch(permission) }
        ) { Text("Требуется разрешение $permissionName") }
    }
}

@Composable
fun PermissionRequired(
    permission: String,
    permissionName: String,
    description: String = "Это разрешение необходимо для корректной работы функции.",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    var hasPermission by useState(PermissionUtils.check(context, permission))

    val requestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasPermission = isGranted }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { hasPermission = PermissionUtils.check(context, permission) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = PermissionUtils.check(context, permission)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (hasPermission) {
        content()
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = colorScheme.primary
                )
                Gap(16)
                Text(
                    text = "Нужно разрешение",
                    style = typography.headlineSmall
                )
                Gap(8)
                Text(
                    text = "$permissionName\n\n$description",
                    textAlign = TextAlign.Center,
                    style = typography.bodyMedium
                )
                Gap(24)
                Button(
                    onClick = {
                        val intent = PermissionUtils.getSpecialPermissionIntent(context, permission)
                        if (intent != null) {
                            settingsLauncher.launch(intent)
                        } else {
                            requestLauncher.launch(permission)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Предоставить")
                }
            }
        }
    }
}

object PermissionUtils {
    @SuppressLint("ServiceCast")
    fun check(context: Context, permission: String): Boolean {
        return when (permission) {
            Manifest.permission.SYSTEM_ALERT_WINDOW -> Settings.canDrawOverlays(context)
            Manifest.permission.REQUEST_INSTALL_PACKAGES -> context.packageManager.canRequestPackageInstalls()
            Manifest.permission.SCHEDULE_EXACT_ALARM -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    alarmManager.canScheduleExactAlarms()
                } else true
            }
            else -> ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getSpecialPermissionIntent(context: Context, permission: String): Intent? {
        val uri = "package:${context.packageName}".toUri()
        return when (permission) {
            Manifest.permission.SYSTEM_ALERT_WINDOW -> Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
            Manifest.permission.REQUEST_INSTALL_PACKAGES -> Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
            Manifest.permission.SCHEDULE_EXACT_ALARM -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, uri)
            } else null
            "APPLICATION_DETAILS" -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
            else -> null
        }
    }
}
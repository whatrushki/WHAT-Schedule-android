package app.what.schedule

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.services.crash.CrashScreen
import app.what.foundation.ui.Show
import app.what.foundation.ui.controllers.rememberDialogController
import app.what.foundation.utils.ShareUtils
import app.what.navigation.core.ProvideGlobalDialog
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.features.dev.DevFeature
import app.what.schedule.ui.theme.AppTheme
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.FrameBug
import org.koin.compose.koinInject
import java.io.File

class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Auditor.critic("app.crash", intent.getStringExtra("CRASH_REPORT") ?: "")

        enableEdgeToEdge()
        setContent {
            // НЕ ПЕРЕМЕЩАТЬ!!
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setNavigationBarContrastEnforced(false)
            }

            AppTheme(koinInject<AppValues>()) {
                ProvideGlobalDialog {
                    val dialog = rememberDialogController()

                    Box {
                        FloatingActionButton(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .zIndex(2f)
                                .systemBarsPadding()
                                .padding(bottom = 20.dp, end = 20.dp),
                            onClick = {
                                dialog.open(full = true) { DevFeature() }
                            }
                        ) {
                            WHATIcons.FrameBug.Show(color = colorScheme.onSecondaryContainer)
                        }

                        CrashScreen(
                            crashReport = intent.getStringExtra("CRASH_REPORT") ?: "",
                            onRestart = { restartApp() },
                            onShare = { shareCrashReport() }
                        )
                    }
                }
            }
        }
    }

    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
        Process.killProcess(Process.myPid())
    }

    private fun shareCrashReport() {
        val crashReport = intent.getStringExtra("CRASH_REPORT") ?: return

        try {
            val file = File(cacheDir, "crash_report.txt")
            file.writeText(crashReport)

            val uris = listOf(
                getFileUri(file),
            ).let {
                if (Auditor.logFile.exists()) it + getFileUri(Auditor.logFile)
                else it
            }

            ShareUtils.shareUris(
                this@CrashActivity,
                ArrayList(uris)
            )
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing report", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileUri(file: File) =
        FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
}
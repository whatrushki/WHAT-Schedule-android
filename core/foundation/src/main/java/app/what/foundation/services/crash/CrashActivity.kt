package app.what.foundation.services.crash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.FileProvider
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.ui.theme.WHATTheme
import java.io.File

class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            // НЕ ПЕРЕМЕЩАТЬ!!
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setNavigationBarContrastEnforced(false)
            }

            WHATTheme {
                CrashScreen(
                    crashReport = intent.getStringExtra("CRASH_REPORT") ?: "",
                    onRestart = { restartApp() },
                    onShare = { shareCrashReport() }
                )
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
                getFileUri(Auditor.logFile)
            )

            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Crash Report")
                putExtra(Intent.EXTRA_TEXT, "Описание ошибки и дополнительная информация")
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Share Crash Report"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing report", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileUri(file: File) = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
}
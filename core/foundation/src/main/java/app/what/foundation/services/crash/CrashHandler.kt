package app.what.foundation.services.crash

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.os.StatFs
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

data class CrashReport(
    val timestamp: Long = System.currentTimeMillis(),
    val exception: Throwable,
    val deviceInfo: DeviceInfo,
    val stackTrace: String,
    val appVersion: String,
    val androidVersion: String,
    val freeMemory: Long,
    val totalMemory: Long,
    val storageInfo: StorageInfo
) {
    fun toText(): String {
        return """
            CRASH REPORT
            ============
            
            Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                .format(Date(timestamp))}
            
            APP INFO:
            ---------
            Version: $appVersion
            Package: ${deviceInfo.packageName}
            
            DEVICE INFO:
            ------------
            Model: ${deviceInfo.model}
            Manufacturer: ${deviceInfo.manufacturer}
            Android Version: $androidVersion
            SDK: ${deviceInfo.sdkVersion}
            
            SYSTEM INFO:
            ------------
            Free Memory: ${freeMemory / 1024 / 1024} MB
            Total Memory: ${totalMemory / 1024 / 1024} MB
            Free Storage: ${storageInfo.freeSpace / 1024 / 1024} MB
            Total Storage: ${storageInfo.totalSpace / 1024 / 1024} MB
            
            EXCEPTION:
            ----------
            Type: ${exception.javaClass.name}
            Message: ${exception.message ?: "No message"}
            
            THREAD INFO:
            ------------
            ${getThreadInfo()}
            
            STACK TRACE:
            ------------
        """.trimIndent().plus("\n$stackTrace")
    }

    private fun getThreadInfo(): String {
        return Thread.currentThread().let { thread ->
            """
            Thread: ${thread.name}
            Priority: ${thread.priority}
            State: ${thread.state}
            ID: ${thread.id}
            """
        }
    }
}

data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val sdkVersion: Int,
    val packageName: String
)

data class StorageInfo(
    val freeSpace: Long,
    val totalSpace: Long
)

class CrashHandler private constructor(
    private val context: Context
) : Thread.UncaughtExceptionHandler {

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    fun setDefaultHandler(handler: Thread.UncaughtExceptionHandler?) {
        defaultHandler = handler
    }

    companion object {
        fun initialize(context: Context) {
            val currentHandler = Thread.getDefaultUncaughtExceptionHandler()

            // Если уже наш обработчик, не делаем ничего
            if (currentHandler is CrashHandler) return

            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(context.applicationContext).apply {
                setDefaultHandler(currentHandler)
            })
        }
    }

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        try {
            val crashReport = createCrashReport(exception)
            saveCrashReport(crashReport)

            val intent = Intent(context, CrashActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("CRASH_REPORT", crashReport.toText())
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.d("d", e.toString())
            // Если что-то пошло не так, вызываем дефолтный обработчик
            defaultHandler?.uncaughtException(thread, exception)
        } finally {
            // Всегда завершаем процесс
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }

    private fun createCrashReport(exception: Throwable): CrashReport {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val storage = context.getExternalFilesDir(null)?.let {
            StatFs(it.path)
        }

        return CrashReport(
            exception = exception,
            deviceInfo = DeviceInfo(
                model = Build.MODEL,
                manufacturer = Build.MANUFACTURER,
                sdkVersion = Build.VERSION.SDK_INT,
                packageName = context.packageName
            ),
            stackTrace = exception.stackTraceToString(),
            appVersion = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: Exception) {
                "Unknown"
            } ?: "Unknown",
            androidVersion = Build.VERSION.RELEASE,
            freeMemory = memoryInfo.availMem,
            totalMemory = memoryInfo.totalMem,
            storageInfo = StorageInfo(
                freeSpace = storage?.availableBlocksLong?.times(storage.blockSizeLong) ?: 0,
                totalSpace = storage?.blockCountLong?.times(storage.blockSizeLong) ?: 0
            )
        )
    }

    private fun saveCrashReport(report: CrashReport) {
        try {
            val file = File(context.filesDir, "crash_report_${report.timestamp}.txt")
            file.writeText(report.toText())
        } catch (e: Exception) {
            // Ignore
        }
    }
}


package app.what.foundation.services.auto_update

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.what.foundation.services.AppLogger.Companion.Auditor
import app.what.foundation.utils.launchIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import ru.rustore.sdk.appupdate.listener.InstallStateUpdateListener
import ru.rustore.sdk.appupdate.manager.RuStoreAppUpdateManager
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory
import ru.rustore.sdk.appupdate.model.AppUpdateInfo
import ru.rustore.sdk.appupdate.model.AppUpdateOptions
import ru.rustore.sdk.appupdate.model.AppUpdateType
import ru.rustore.sdk.appupdate.model.InstallStatus
import ru.rustore.sdk.appupdate.model.UpdateAvailability
import kotlin.coroutines.resume

class RuStoreUpdateManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : AppUpdateManager {

    private val updateManager: RuStoreAppUpdateManager by lazy {
        RuStoreAppUpdateManagerFactory.create(context)
    }

    private var _rawUpdateInfo: AppUpdateInfo? = null   // храним оригинал, т.к. он одноразовый

    override var updateInfo by mutableStateOf<UpdateInfo?>(null)
        private set

    private val _downloadState = mutableStateOf<DownloadState>(DownloadState.Idle)
    override val downloadState: DownloadState get() = _downloadState.value

    private var installStateListener: InstallStateUpdateListener? = null

    init {
        registerListener()
        scope.launchIO {
            Auditor.debug("d", "start check rustore")
            checkForUpdates()
        }
    }

    override suspend fun checkForUpdates(): UpdateResult = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { cont ->
            updateManager.getAppUpdateInfo()
                .addOnSuccessListener { appUpdateInfo ->
                    _rawUpdateInfo = appUpdateInfo  // сохраняем свежий объект

                    if (appUpdateInfo.updateAvailability == UpdateAvailability.UPDATE_AVAILABLE) {
                        updateInfo = UpdateInfo(
                            version = appUpdateInfo.availableVersionName,
                            releaseNotes = appUpdateInfo.whatsNew
                            // fileSize и т.д. — в RuStore обычно недоступны
                        )
                        _downloadState.value = DownloadState.Idle
                        cont.resume(UpdateResult.Available(updateInfo!!))
                    } else {
                        cont.resume(UpdateResult.UpToDate)
                    }
                }
                .addOnFailureListener { e ->
                    Auditor.err("d","RuStore check failed", e)
                    cont.resume(UpdateResult.Error(e.message ?: "RuStore error"))
                }
        }
    }

    override fun handleAction() {
        val rawInfo = _rawUpdateInfo ?: return
        when (downloadState) {
            is DownloadState.Idle,
            is DownloadState.Error -> startFlexibleUpdate(rawInfo)
            is DownloadState.Completed -> {
                // В RuStore после DOWNLOADED нужно явно вызвать completeUpdate()
                updateManager.completeUpdate(AppUpdateOptions.Builder()
                    .appUpdateType(AppUpdateType.FLEXIBLE)
                    .build())
            }
            else -> { /* уже в процессе */ }
        }
    }

    private fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo) {
        if (context !is Activity) {
            Auditor.err("d", "RuStore update requires Activity context")
            _downloadState.value = DownloadState.Error("Требуется Activity")
            return
        }

        val options = AppUpdateOptions.Builder()
            .appUpdateType(AppUpdateType.FLEXIBLE)
            .build()

        updateManager.startUpdateFlow(appUpdateInfo, options)
            .addOnSuccessListener { /* можно логировать */ }
            .addOnFailureListener { e ->
                Auditor.err("d", "RuStore startUpdateFlow failed", e)
                _downloadState.value = DownloadState.Error(e.message ?: "Ошибка запуска обновления")
            }
    }

    private fun registerListener() {
        installStateListener = InstallStateUpdateListener { state ->
            when (state.installStatus) {
                InstallStatus.PENDING -> {
                    _downloadState.value = DownloadState.Preparing
                }
                InstallStatus.DOWNLOADING -> {
                    val total = state.totalBytesToDownload
                    val progress = if (total > 0) {
                        (state.bytesDownloaded * 100 / total).toInt().coerceIn(0, 100)
                    } else 0
                    _downloadState.value = DownloadState.Downloading(progress)
                }
                InstallStatus.DOWNLOADED -> {
                    _downloadState.value = DownloadState.Completed()
                    // Рекомендуется: сразу предложить установить или вызвать completeUpdate()
                    // updateManager.completeUpdate()
                }
                InstallStatus.FAILED -> {
                    _downloadState.value = DownloadState.Error("Ошибка установки")
                }
                else -> {}
            }
        }

        updateManager.registerListener(installStateListener!!)
    }

    override fun cancelDownload() {
        // RuStore SDK не предоставляет явной отмены загрузки в большинстве версий
        // Можно только сбросить состояние
        _downloadState.value = DownloadState.Idle
    }

    override fun release() {
        installStateListener?.let { updateManager.unregisterListener(it) }
        scope.cancel()
    }
}
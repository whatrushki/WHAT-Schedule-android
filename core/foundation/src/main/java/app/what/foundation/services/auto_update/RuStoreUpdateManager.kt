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

    private var _rawUpdateInfo: AppUpdateInfo? = null

    override var updateInfo by mutableStateOf<UpdateInfo?>(null)
        private set

    private val _downloadState = mutableStateOf<DownloadState>(DownloadState.Idle)
    override val downloadState: DownloadState get() = _downloadState.value

    private var installStateListener: InstallStateUpdateListener? = null

    init {
        Auditor.debug("RuStoreUpdate", "Инициализация менеджера → регистрируем listener и запускаем проверку обновлений")
        registerListener()
        scope.launchIO {
            Auditor.debug("RuStoreUpdate", "Запуск асинхронной проверки обновлений (IO)")
            checkForUpdates()
        }
    }

    override suspend fun checkForUpdates(): UpdateResult = withContext(Dispatchers.IO) {
        Auditor.info("RuStoreUpdate", "→ checkForUpdates() начата")

        suspendCancellableCoroutine { cont ->
            updateManager.getAppUpdateInfo()
                .addOnSuccessListener { appUpdateInfo ->
                    _rawUpdateInfo = appUpdateInfo

                    val availability = appUpdateInfo.updateAvailability
                    val version = appUpdateInfo.availableVersionName
                    val whatsNew = appUpdateInfo.whatsNew.take(80)

                    Auditor.debug(
                        "RuStoreUpdate",
                        "getAppUpdateInfo → успех | " +
                                "availability = $availability | " +
                                "version = $version | " +
                                "whatsNew = \"$whatsNew\" | " +
                                "bytesDownload = ${appUpdateInfo.fileSize}"
                    )

                    if (availability == UpdateAvailability.UPDATE_AVAILABLE) {
                        updateInfo = UpdateInfo(
                            version = version,
                            releaseNotes = appUpdateInfo.whatsNew
                        )
                        _downloadState.value = DownloadState.Idle

                        Auditor.info("RuStoreUpdate", "Обновление доступно → UpdateInfo установлен, состояние → Idle")
                        cont.resume(UpdateResult.Available(updateInfo!!))
                    } else {
                        Auditor.info("RuStoreUpdate", "Обновление НЕ доступно (availability = $availability)")
                        cont.resume(UpdateResult.UpToDate)
                    }
                }
                .addOnFailureListener { e ->
                    Auditor.err("RuStoreUpdate", "getAppUpdateInfo → провал", e)
                    cont.resume(UpdateResult.Error(e.message ?: "RuStore неизвестная ошибка"))
                }
        }.also {
            Auditor.debug("RuStoreUpdate", "← checkForUpdates() завершена → $it")
        }
    }

    override fun handleAction() {
        val raw = _rawUpdateInfo
        if (raw == null) {
            Auditor.warn("RuStoreUpdate", "handleAction() → _rawUpdateInfo == null, ничего не делаем")
            return
        }

        Auditor.info(
            "RuStoreUpdate",
            "handleAction() → текущее состояние = ${downloadState.javaClass.simpleName}, " +
                    "availability = ${raw.updateAvailability}"
        )

        when (downloadState) {
            is DownloadState.Idle,
            is DownloadState.Error -> {
                Auditor.debug("RuStoreUpdate", "→ запускаем flexible update (Idle / Error)")
                startFlexibleUpdate(raw)
            }

            is DownloadState.Completed -> {
                Auditor.info("RuStoreUpdate", "Состояние COMPLETED → вызываем completeUpdate(FLEXIBLE)")
                updateManager.completeUpdate(
                    AppUpdateOptions.Builder()
                        .appUpdateType(AppUpdateType.FLEXIBLE)
                        .build()
                )
                // После вызова completeUpdate приложение обычно перезапустится автоматически
            }

            else -> {
                Auditor.debug("RuStoreUpdate", "handleAction → процесс уже идёт (${downloadState.javaClass.simpleName}), игнорируем")
            }
        }
    }

    private fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo) {
        if (context !is Activity) {
            Auditor.err("RuStoreUpdate", "startFlexibleUpdate → context не Activity → невозможно начать обновление")
            _downloadState.value = DownloadState.Error("Требуется контекст Activity")
            return
        }

        val options = AppUpdateOptions.Builder()
            .appUpdateType(AppUpdateType.FLEXIBLE)
            .build()

        Auditor.info(
            "RuStoreUpdate",
            "→ startUpdateFlow() | version = ${appUpdateInfo.availableVersionName} | " +
                    "bytes = ${appUpdateInfo.fileSize}"
        )

        updateManager.startUpdateFlow(appUpdateInfo, options)
            .addOnSuccessListener {
                Auditor.debug("RuStoreUpdate", "startUpdateFlow → успешно запущен поток")
            }
            .addOnFailureListener { e ->
                Auditor.err("RuStoreUpdate", "startUpdateFlow → ошибка запуска", e)
                _downloadState.value = DownloadState.Error(e.message ?: "Не удалось запустить загрузку")
            }
    }

    private fun registerListener() {
        Auditor.debug("RuStoreUpdate", "Регистрация InstallStateUpdateListener")

        installStateListener = InstallStateUpdateListener { state ->
            val status = state.installStatus
            val bytes = state.bytesDownloaded
            val total = state.totalBytesToDownload
            val percent = if (total > 0) (bytes * 100 / total).coerceIn(0, 100) else 0

            Auditor.debug(
                "RuStoreUpdate",
                "← InstallStateUpdate | status = $status | " +
                        "bytes = $bytes / $total ($percent%) | " +
                        "errorCode = ${state.installErrorCode}"
            )

            when (status) {
                InstallStatus.PENDING -> {
                    Auditor.info("RuStoreUpdate", "→ PENDING (ожидание начала загрузки)")
                    _downloadState.value = DownloadState.Preparing
                }

                InstallStatus.DOWNLOADING -> {
                    Auditor.debug("RuStoreUpdate", "→ DOWNLOADING ($percent%)")
                    _downloadState.value = DownloadState.Downloading(percent.toInt())
                }

                InstallStatus.DOWNLOADED -> {
                    Auditor.info("RuStoreUpdate", "→ DOWNLOADED (файл загружен, готов к установке)")
                    _downloadState.value = DownloadState.Completed()
                    // Здесь можно автоматически предложить установить или ждать нажатия пользователя
                }

                InstallStatus.FAILED -> {
                    Auditor.err("RuStoreUpdate", "→ FAILED (ошибка загрузки/установки)")
                    _downloadState.value = DownloadState.Error("Ошибка загрузки или установки")
                }

                InstallStatus.UNKNOWN -> {
                    Auditor.warn("RuStoreUpdate", "→ UNKNOWN (сброс состояния или прерывание)")
                    // Часто после отмены пользователем или ошибки
                }

                else -> {
                    Auditor.debug("RuStoreUpdate", "→ Необработанный installStatus = $status")
                }
            }
        }

        updateManager.registerListener(installStateListener!!)
        Auditor.debug("RuStoreUpdate", "Listener успешно зарегистрирован")
    }

    override fun cancelDownload() {
        Auditor.warn("RuStoreUpdate", "cancelDownload() вызван → RuStore SDK не поддерживает явную отмену загрузки")
        Auditor.debug("RuStoreUpdate", "Сбрасываем UI-состояние в Idle (реальная загрузка может продолжаться)")
        _downloadState.value = DownloadState.Idle
    }

    override fun release() {
        Auditor.info("RuStoreUpdate", "release() → отписываемся от listener и отменяем scope")
        installStateListener?.let {
            updateManager.unregisterListener(it)
            Auditor.debug("RuStoreUpdate", "Listener отписан")
        }
        scope.cancel()
        Auditor.debug("RuStoreUpdate", "CoroutineScope отменён")
    }
}
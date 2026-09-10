package app.what.schedule.desktop.ui

import androidx.compose.runtime.Composable
import app.what.compose.App
import app.what.schedule.desktop.ui.components.UpdateBanner
import app.what.schedule.desktop.updater.DesktopAppUpdateManager
import io.ktor.client.HttpClient

@Composable
fun DesktopApp(httpClient: HttpClient, updateManager: DesktopAppUpdateManager) {
    App(
        httpClient = httpClient,
        headerBanner = {
            UpdateBanner(
                updateInfo = updateManager.updateInfo,
                onUpdateClick = { updateManager.handleAction() }
            )
        }
    )
}

package app.what.schedule.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import app.what.schedule.desktop.ui.DesktopApp
import app.what.schedule.desktop.updater.DesktopAppUpdateManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun main() = application {
    val httpClient = remember {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
    val updateManager = remember { DesktopAppUpdateManager(httpClient) }

    Window(
        onCloseRequest = {
            updateManager.release()
            exitApplication()
        },
        title = "WHAT Schedule",
        state = WindowState(size = DpSize(1100.dp, 750.dp))
    ) {
        DesktopApp(httpClient, updateManager)
    }
}

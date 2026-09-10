package app.what.schedule.wasm

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import app.what.schedule.wasm.ui.WebApp
import app.what.schedule.wasm.updater.WasmUpdateManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    val updateManager = WasmUpdateManager(httpClient)

    CanvasBasedWindow(title = "WHAT Schedule Web", canvasElementId = "ComposeTarget") {
        WebApp(httpClient, updateManager)
    }
}

package app.what.schedule.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    fun create(
        engine: HttpClientEngine? = null,
        customUserAgent: String = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36",
        timeoutMillis: Long = 15_000
    ): HttpClient {
        val config: io.ktor.client.HttpClientConfig<*>.() -> Unit = {
            install(HttpTimeout) {
                requestTimeoutMillis = timeoutMillis
                connectTimeoutMillis = timeoutMillis
                socketTimeoutMillis = timeoutMillis
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                    coerceInputValues = true
                })
            }
            defaultRequest {
                header(HttpHeaders.UserAgent, customUserAgent)
                header(HttpHeaders.Accept, "text/html,application/xhtml+xml,application/xml;q=0.9,application/json,*/*;q=0.8")
                header(HttpHeaders.AcceptLanguage, "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
            }
        }
        return if (engine != null) HttpClient(engine, config) else HttpClient(config)
    }
}

package app.what.schedule.core.network

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement

inline fun <reified T : Any> HttpRequestBuilder.setData(
    obj: T,
    json: Json = Json
) {
    val jsonElement = json.encodeToJsonElement(obj)
    if (jsonElement is JsonObject) {
        jsonElement.forEach { (key, value) ->
            if (value is JsonPrimitive) {
                parameter(key, value.content)
            }
        }
    }
}

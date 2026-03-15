package app.what.schedule.utils

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
    val jsonObject = jsonElement as JsonObject
    jsonObject.forEach {
        when (it.value) {
            is JsonPrimitive -> parameter(it.key, it.value.toString())
            else -> Unit
        }
    }
}
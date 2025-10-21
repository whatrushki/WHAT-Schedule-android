package app.what.foundation.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import java.io.File


suspend fun HttpClient.downloadFile(
    url: String,
    outputFile: File
) {
    val response = get(url)

    response.body<ByteReadChannel>().let { channel ->
        outputFile.outputStream().use { output ->
            channel.copyTo(output)
        }
    }
}
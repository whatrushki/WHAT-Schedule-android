package app.what.foundation.utils

import kotlinx.coroutines.delay


suspend fun retry(times: Int, sleep: Long? =null, block: suspend () -> Unit) {
    repeat(times) { attempt ->
        try {
            block()
            return@retry
        } catch (e: Exception) {
            if (attempt == times - 1) throw e
            delay(sleep ?: attempt.plus(1).times(100).toLong())
        }
    }
}
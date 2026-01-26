package app.what.foundation.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

fun CoroutineScope.launchIO(block: suspend CoroutineScope.() -> Unit) =
    launch(IO, block = block)

fun CoroutineScope.delayLaunch(timeMillis: Long, block: suspend () -> Unit) = launch {
    delay(timeMillis)
    block()
}

fun <T> CoroutineScope.asyncLazy(
    context: CoroutineContext = IO,
    block: suspend () -> T
): Lazy<Deferred<T>> = lazy {
    async(context, start = CoroutineStart.LAZY) { block() }
}

fun CoroutineScope.launchSafe(
    context: CoroutineContext = IO,
    retryCount: Int = 0,
    debug: Boolean = false,
    onFailure: suspend CoroutineScope.(Exception) -> Unit = {},
    onFinally: suspend CoroutineScope.() -> Unit = {},
    block: suspend CoroutineScope.(attempt: Int) -> Unit
): Job = launch(context) {
    try {
        repeat(retryCount + 1) { attempt ->
            try {
                block(attempt)
                return@launch
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (attempt != retryCount) return@repeat
                if (debug) throw e else onFailure(e)
            }
        }
    } finally {
        onFinally()
    }
}
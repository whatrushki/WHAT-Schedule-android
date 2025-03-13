package app.what.foundation.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

fun suspendCall(
    dispatcher: CoroutineDispatcher = IO,
    scope: CoroutineScope = CoroutineScope(dispatcher),
    block: SE<CoroutineScope>
) = scope.launch(context = dispatcher, block = block)

fun suspendCall(dispatcher: CoroutineDispatcher = IO, block: SE<CoroutineScope>) =
    CoroutineScope(dispatcher).launch(block = block)

fun suspendCall(scope: CoroutineScope, dispatcher: CoroutineDispatcher = IO, block: SE<CoroutineScope>) =
    scope.launch(context = dispatcher, block = block)

fun doAfter(millis: Long = 3000L, block: SE<CoroutineScope>) =
    suspendCall { delay(millis); block() }


fun safeExecute(
    context: CoroutineContext = IO,
    scope: CoroutineScope = CoroutineScope(context),
    retryCount: Int = 0,
    finally: SE<CoroutineScope> = {},
    failure: SE1<CoroutineScope, Exception> = {},
    block: SE1<CoroutineScope, Int> = {}
) = scope.launch(context) {

    val doRequest: SRE1<CoroutineScope, Int, Boolean> = {
        try {
            block(it)
            true
        } catch (e: Exception) {
            if (it == retryCount) failure(e)
            false
        }
    }

    for (i in 0..retryCount) if (doRequest(i)) break
    finally()
}
package app.what.foundation.utils

fun measureTime(block: () -> Unit): Long {
    val startTime = System.nanoTime()
    block()
    val endTime = System.nanoTime()
    return endTime - startTime
}

suspend fun suspendMeasureTime(block: suspend () -> Unit): Long {
    val startTime = System.nanoTime()
    block()
    val endTime = System.nanoTime()
    return endTime - startTime
}
package app.what.foundation.utils

import androidx.compose.runtime.Composable

fun <T : Any> clazy(block: @Composable () -> T) = ComposableLazy(block)

class ComposableLazy<T : Any>(
    private val block: @Composable () -> T
) {
    var value: T? = null

    @Composable
    fun calculate() {
        if (value == null) value = block()
    }

    fun get(): T = value.orThrow
}
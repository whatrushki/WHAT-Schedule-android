package app.what.foundation.core

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Monitor<T : Any?>(
    initValue: T,
    private val compare: Boolean
) : ReadWriteProperty<Any?, T> {
    companion object {
        fun <T : Any?> monitored(initValue: T, compare: Boolean = true) =
            Monitor(initValue, compare)
    }

    private val state: MutableState<T> = mutableStateOf(initValue)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return state.value
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (compare && state.value == value) return
        state.value = value
    }
}
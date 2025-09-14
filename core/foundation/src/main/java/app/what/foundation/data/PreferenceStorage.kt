package app.what.foundation.data

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class PreferenceStorage(
    private val prefs: SharedPreferences,
    private val preferencesFlow: MutableSharedFlow<String>
) {

    class Value<T : Any>(
        private val prefs: SharedPreferences,
        private val preferencesFlow: MutableSharedFlow<String>,
        private val key: String,
        private val defaultValue: T?,
        private val serializer: KSerializer<T>
    ) {
        fun get(): T? = prefs
            .getString(key, null)
            ?.let { Json.decodeFromString(serializer, it) }
            ?: defaultValue

        fun set(value: T?) {
            prefs.edit {
                putString(
                    key,
                    if (value == null) null
                    else Json.encodeToString(serializer, value)
                )
                apply()
            }

            preferencesFlow.tryEmit(key)
        }

        fun observe(): Flow<T?> = preferencesFlow
            .filter { it == key }
            .map { get() }
            .onStart { emit(get()) }
            .distinctUntilChanged()

        @Composable
        fun collect() = observe().collectAsState(get())
    }


    fun <T : Any> createValue(
        key: String,
        defaultValue: T?,
        serializer: KSerializer<T>
    ): Value<T> {
        return Value(prefs, preferencesFlow, key, defaultValue, serializer)
    }
}

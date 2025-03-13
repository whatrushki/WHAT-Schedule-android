package app.what.foundation.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.what.foundation.ui.useState

@Composable
fun <T> rememberPreference(key: String, defaultValue: T, get: (SharedPreferences) -> T?): T {
    val context = LocalContext.current
    val prefs =
        remember { context.getSharedPreferences("MY_APP_PREFERENCES", Context.MODE_PRIVATE) }
    val (state, setState) = useState(get(prefs) ?: defaultValue)

    LaunchedEffect(key) {
        prefs.registerOnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) {
                setState(get(prefs) ?: defaultValue)
            }
        }
    }

    return state
}

@Composable
fun rememberStringPreference(key: String, defaultValue: String): String {
    return rememberPreference(key, defaultValue) { it.getString(key, defaultValue) }
}

@Composable
fun rememberLongPreference(key: String, defaultValue: Long): Long {
    return rememberPreference(key, defaultValue) { it.getLong(key, defaultValue) }
}

@Composable
fun rememberBooleanPreference(key: String, defaultValue: Boolean): Boolean {
    return rememberPreference(key, defaultValue) { it.getBoolean(key, defaultValue) }
}

@Composable
fun rememberIntPreference(key: String, defaultValue: Int): Int {
    return rememberPreference(key, defaultValue) { it.getInt(key, defaultValue) }
}

@Composable
fun rememberFloatPreference(key: String, defaultValue: Float): Float {
    return rememberPreference(key, defaultValue) { it.getFloat(key, defaultValue) }
}

@Composable
inline fun <reified T : Enum<T>> rememberEnumPreference(key: String, defaultValue: T): T {
    return rememberPreference(key, defaultValue) { enumValues<T>().getOrNull(it.getInt(key, defaultValue.ordinal)) }
}
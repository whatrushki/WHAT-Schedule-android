package app.what.foundation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun <T : Any?> useState(initialValue: T): MutableState<T> {
    val state = remember { mutableStateOf(initialValue) }
    return state
}

@Composable
fun <T : Any?> useSave(initialValue: T): MutableState<T> {
    val state = rememberSaveable { mutableStateOf(initialValue) }
    return state
}

@Composable
fun <T : Any?> useChange(
    initialValue: T,
    time: Long = 10,
    scope: CoroutineScope = rememberCoroutineScope(),
    block: (T) -> T
): MutableState<T> {
    val state = remember { mutableStateOf(initialValue) }
    LaunchedEffect(Unit) {
        scope.launch(IO) {
            delay(time * 1000)
            state.value = block(state.value)
        }
    }

    return state
}
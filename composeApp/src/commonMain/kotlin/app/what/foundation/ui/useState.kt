package app.what.foundation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun <T : Any?> useState(initialValue: T): MutableState<T> {
    return remember { mutableStateOf(initialValue) }
}

@Composable
fun <T> useState(initialValue: T, key: Any? = Unit): MutableState<T> {
    return remember(key) { mutableStateOf(initialValue) }
}

@Composable
fun <T> useState(initialValue: T, vararg keys: Any?): MutableState<T> {
    return remember(*keys) { mutableStateOf(initialValue) }
}

@Composable
fun <T : Any?> useStateList(): SnapshotStateList<T> {
    return remember { mutableStateListOf<T>() }
}

@Composable
fun <T : Any?> useStateList(vararg initialValue: T): SnapshotStateList<T> {
    return remember { mutableStateListOf(*initialValue) }
}

@Composable
fun <T> useSave(initialValue: T, vararg inputs: Any?): MutableState<T> {
    return rememberSaveable(inputs = inputs) {
        mutableStateOf(initialValue)
    }
}

@Composable
fun <T> useChange(
    initialValue: T,
    delayMillis: Long = 10000L,
    block: (T) -> T
): State<T> {
    val state = remember { mutableStateOf(initialValue) }
    val currentBlock by rememberUpdatedState(block)

    LaunchedEffect(Unit) {
        while (isActive) {
            state.value = currentBlock(state.value)
            delay(delayMillis)
        }
    }

    return state
}

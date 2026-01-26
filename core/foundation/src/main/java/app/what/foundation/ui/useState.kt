package app.what.foundation.ui

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

@Composable
fun <T : Any?> useState(initialValue: T): MutableState<T> {
    val state = remember { mutableStateOf(initialValue) }
    return state
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
    val state = remember { mutableStateListOf<T>() }
    return state
}

@Composable
fun <T : Any?> useStateList(vararg initialValue: T): SnapshotStateList<T> {
    val state = remember { mutableStateListOf(*initialValue) }
    return state
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
    val isAppInForeground by rememberIsAppInForeground()

    val currentBlock by rememberUpdatedState(block)

    LaunchedEffect(isAppInForeground) {
        while (isAppInForeground) {
            state.value = currentBlock(state.value)
            delay(delayMillis)
        }
    }

    return state
}

@Composable
fun rememberIsAppInForeground(): State<Boolean> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) state.value = true
            else if (event == Lifecycle.Event.ON_PAUSE) state.value = false
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return state
}
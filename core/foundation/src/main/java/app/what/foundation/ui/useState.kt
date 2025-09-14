package app.what.foundation.ui

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
fun <T : Any?> useSave(initialValue: T): MutableState<T> {
    val state = rememberSaveable { mutableStateOf(initialValue) }
    return state
}

@Composable
fun <T : Any?> useChange(
    initialValue: T,
    time: Long = 10,
    block: (T) -> T
): MutableState<T> {
    val state = remember { mutableStateOf(initialValue) }
    val isAppInForeground by rememberIsAppInForeground()

    LaunchedEffect(Unit) {
        while (true) {
            delay(time * 1000)
            if (isAppInForeground) {
                state.value = block(state.value)
            }
        }
    }

    return state
}

@Composable
fun rememberIsAppInForeground(): MutableState<Boolean> {
    val context = LocalContext.current
    val isForeground = remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val app = context.applicationContext as Application

        val callback = object :  Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                isForeground.value = true
            }

            override fun onActivitySaveInstanceState(
                activity: Activity,
                outState: Bundle
            ) {

            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivityCreated(
                activity: Activity,
                savedInstanceState: Bundle?
            ) {}

            override fun onActivityDestroyed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {
                isForeground.value = false
            }
        }

        app.registerActivityLifecycleCallbacks(callback)

        onDispose {
            app.unregisterActivityLifecycleCallbacks(callback)
        }
    }

    return isForeground
}
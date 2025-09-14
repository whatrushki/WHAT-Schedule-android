package app.what.foundation.utils

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope

@SuppressLint("ComposableNaming")
@Composable
fun <T> subscribe(value: T, scope: CoroutineScope = rememberCoroutineScope(), block: (T) -> Unit) {
//    scope.launch {
//        block(value)
//    }
}
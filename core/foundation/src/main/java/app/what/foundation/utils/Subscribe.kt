package app.what.foundation.utils

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("ComposableNaming")
@Composable
fun <T> subscribe(value: T, scope: CoroutineScope = rememberCoroutineScope(), block: (T) -> Unit) {
//    scope.launch {
//        block(value)
//    }
}
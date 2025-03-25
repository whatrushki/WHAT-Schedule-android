package app.what.foundation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun <T> T.freeze(): T = remember { this }

@Composable
fun <T> T.freeze(key: Any): T = remember(key) { this }

@Composable
fun <T> T.freeze(key1: Any, key2: Any): T = remember(key1, key2) { this }

@Composable
fun <T> T.freeze(key1: Any, key2: Any, key3: Any): T = remember(key1, key2, key3) { this }

@Composable
fun <T> T.freeze(vararg keys: Any): T = remember(keys) { this }
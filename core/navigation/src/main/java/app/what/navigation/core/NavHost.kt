package app.what.navigation.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost


@Composable
fun NavigationHost(
    modifier: Modifier = Modifier,
    start: NavProvider,
    navigator: Navigator = rememberHostNavigator(),
    registry: Registry
) = CompositionLocalProvider(
    LocalNavController provides navigator
) {
    NavHost(
        modifier = modifier,
        startDestination = start,
        navController = navigator.c,
        builder = registry
    )
}

package app.what.navigation.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import app.what.foundation.utils.orThrow

val LocalNavController = compositionLocalOf<Navigator?> { null }

data class Navigator(
    val parent: Navigator?,
    val c: NavHostController
)

@Composable
fun rememberHostNavigator(
    parent: Navigator? = LocalNavController.current
): Navigator = Navigator(parent, rememberNavController())

@Composable
fun rememberNavigator(): Navigator =
    LocalNavController.current.orThrow

@Composable
fun rememberNavigator(level: Int): Navigator {
    val current = LocalNavController.current
    var parentNavigator = current

    for (i in 1 until level) {
        parentNavigator ?: break
        parentNavigator = parentNavigator.parent
    }

    return parentNavigator.orThrow
}

package app.what.navigation.core

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlin.reflect.KClass

typealias Registry = NavGraphBuilder.() -> Unit

inline fun <reified P : NavProvider, S : NavComponent<P>> NavGraphBuilder.register(screen: KClass<S>) {
    composable<P> {
        val s = remember { screen.constructors.first().call(it.toRoute<P>()) }
        s.content(Modifier)
    }
}
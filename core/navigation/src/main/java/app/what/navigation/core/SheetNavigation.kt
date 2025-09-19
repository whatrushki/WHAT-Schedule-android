package app.what.navigation.core

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.what.foundation.ui.controllers.LocalSheetController
import app.what.foundation.ui.controllers.SheetController
import app.what.foundation.ui.controllers.rememberSheetHostController
import kotlin.reflect.KClass

@Composable
fun rememberSheetNavigator() = LocalSheetNavigator.current

class SheetNavigator(
    private val sheetController: SheetController,
    private val graph: SheetNavGraph
) {
    private var _stackEntries: MutableList<SheetProvider> = mutableListOf()
    val stackEntries: List<SheetProvider> get() = _stackEntries

    fun <T : SheetProvider> navigateTo(
        provider: T,
        launchSingleTop: Boolean = false
    ) {
        if (launchSingleTop) {
            _stackEntries.removeIf { it::class != provider::class }
        }

        if (_stackEntries.lastOrNull() != provider) {
            _stackEntries.add(provider)
        }

        navigateInternal(provider)
    }

    fun navigateUp() {
        _stackEntries.removeAt(_stackEntries.lastIndex)

        val provider = _stackEntries.lastOrNull()
            ?: return sheetController.let {
                it.content = {}
                it.close()
            }

        navigateInternal(provider)
    }

    private fun navigateInternal(provider: SheetProvider) = sheetController.apply {
        val content = graph.get(provider::class)

        this.content = { content(provider) }
        configure(provider)
        open(true)
    }

    private fun SheetController.configure(provider: SheetProvider) {
        this.cancellable = provider.cancellable
    }
}

fun sheetGraph(block: SheetGraphBuilder.() -> Unit) =
    SheetGraphBuilder().apply(block).build()

class SheetNavGraph internal constructor(
    val registry: MutableMap<KClass<out SheetProvider>, @Composable (Any) -> Unit>
) {
    companion object {
        val Empty
            get() = SheetNavGraph(mutableMapOf())
    }

    inline fun <reified T : SheetProvider> get(): @Composable (Any) -> Unit {
        return registry[T::class] ?: error("invalid sheet provider key")
    }

    fun <T : SheetProvider> get(key: KClass<T>): @Composable (Any) -> Unit {
        return registry[key] ?: error("invalid sheet provider key")
    }
}

class SheetGraphBuilder {
    private val _navRegistry = mutableMapOf<KClass<out SheetProvider>, @Composable (Any) -> Unit>()
    val navRegistry get() = _navRegistry

    inline fun <reified T : SheetProvider> composable(noinline content: @Composable (Any) -> Unit) {
        navRegistry[T::class] = content
    }

    fun build(): SheetNavGraph {
        return SheetNavGraph(navRegistry)
    }
}

interface SheetProvider {
    val cancellable: Boolean
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvideGlobalSheet(
    controller: SheetController = rememberSheetHostController(),
    navGraph: SheetNavGraph = SheetNavGraph.Empty,
    transitionSpec: AnimatedContentTransitionScope<@Composable () -> Unit>.() -> ContentTransform = {
        fadeIn() togetherWith fadeOut()
    },
    content: @Composable () -> Unit
) = CompositionLocalProvider(
    LocalSheetController provides controller,
    LocalSheetNavigator provides SheetNavigator(controller, navGraph)
) {
    val state = rememberModalBottomSheetState {
        if (it != SheetValue.Hidden) true
        else controller.cancellable
    }

    LaunchedEffect(Unit) { controller.setSheetState(state) }

    content()

    if (controller.opened) ModalBottomSheet(
        onDismissRequest = controller::close,
        sheetState = state
    ) {
        BackHandler {
            if (controller.cancellable) controller.animateClose()
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = controller.content,
                transitionSpec = transitionSpec, label = "AnimatedSheetContent"
            ) { sheetContent -> sheetContent() }
        }
    }
}

val LocalSheetNavigator = staticCompositionLocalOf<SheetNavigator> { error("непон") }


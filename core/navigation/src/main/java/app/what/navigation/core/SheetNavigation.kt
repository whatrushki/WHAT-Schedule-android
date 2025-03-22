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
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.what.foundation.core.Monitor.Companion.monitored
import app.what.foundation.ui.useState
import app.what.foundation.utils.orThrow
import app.what.foundation.utils.retry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KClass


@Composable
fun <T : SheetProvider> SheetNavHost(
    modifier: Modifier = Modifier,
    navigator: SheetNavigator,
    start: T,
    registry: SheetGraphBuilder.() -> Unit,
    content: @Composable () -> Unit
) {

}

@Composable
fun rememberSheetNavigator(sheetController: SheetController) =
    remember { SheetNavigator(sheetController) }

class SheetNavigator(
    private val sheetController: SheetController
) {
    private var _graph: SheetNavGraph? = null
    var graph: SheetNavGraph
        get() = checkNotNull(_graph) { "sheet graph is not set" }
        set(graph) {
            _graph = graph
        }

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
        open()
    }

    private fun SheetController.configure(provider: SheetProvider) {
        this.cancellable = provider.cancellable
    }
}

fun sheetGraph(block: SheetGraphBuilder.() -> Unit) =
    SheetGraphBuilder().apply(block).build()

class SheetNavGraph(
    val registry: MutableMap<KClass<out SheetProvider>, @Composable (Any) -> Unit>
) {
    inline fun <reified T : SheetProvider> get(): @Composable (Any) -> Unit {
        return registry[T::class].orThrow
    }

    fun <T : SheetProvider> get(key: KClass<T>): @Composable (Any) -> Unit {
        return registry[key].orThrow
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
    transitionSpec: AnimatedContentTransitionScope<@Composable () -> Unit>.() -> ContentTransform = {
        fadeIn() togetherWith fadeOut()
    },
    content: @Composable () -> Unit
) = CompositionLocalProvider(
    LocalSheetController provides controller
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
        BackHandler { controller.animateClose() }

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

@Composable
fun rememberSheetController(): SheetController = LocalSheetController.current

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSheetHostController(
    start: @Composable () -> Unit = {},
    scope: CoroutineScope = rememberCoroutineScope()
): SheetController {
    var sheetState by useState<SheetState?>(null)

    return remember {
        object : SheetController {
            override var content by monitored(start)
            override var cancellable by monitored(true)
            override var opened by monitored(false)

            override fun setSheetState(state: SheetState) {
                sheetState = state
            }

            override fun open(
                full: Boolean,
                cancellable: Boolean,
                content: @Composable () -> Unit
            ) {
                this.content = content
                this.cancellable = cancellable
                open(full)
            }

            override fun open(full: Boolean) {
                opened = true
                scope.launch {
                    delay(300)
                    // TODO: сумашедший костыль, исправить по возможности
                    retry(9, 100) {
                        if (full) sheetState?.expand()
                    }
                }
            }

            override fun close() {
                opened = false
            }

            override fun animateClose() {
                scope.launch { sheetState?.hide() }
                    .invokeOnCompletion { close() }
            }
        }
    }
}


val LocalSheetController = staticCompositionLocalOf<SheetController> { error("непон") }

interface SheetController {
    val opened: Boolean
    var cancellable: Boolean
    var content: @Composable () -> Unit

    fun open(full: Boolean = false)
    fun open(full: Boolean = false, cancellable: Boolean = true, content: @Composable () -> Unit)
    fun close()
    fun animateClose()

    @OptIn(ExperimentalMaterial3Api::class)
    fun setSheetState(state: SheetState)
}
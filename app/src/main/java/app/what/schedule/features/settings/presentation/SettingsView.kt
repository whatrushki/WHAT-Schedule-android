package app.what.schedule.features.settings.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.core.Listener
import app.what.foundation.core.UIComponent
import app.what.foundation.data.settings.dependsOn
import app.what.foundation.data.settings.types.asColorPalette
import app.what.foundation.data.settings.types.asSingleChoice
import app.what.foundation.data.settings.types.asSwitch
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.animations.AnimatedEnter
import app.what.foundation.ui.bclick
import app.what.foundation.ui.controllers.rememberDialogController
import app.what.foundation.ui.keyboardAsState
import app.what.foundation.ui.useState
import app.what.schedule.controllers
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.local.settings.ThemeStyle
import app.what.schedule.data.local.settings.ThemeType
import app.what.schedule.features.settings.domain.models.SettingsEvent
import app.what.schedule.features.settings.domain.models.SettingsState
import app.what.schedule.features.settings.presentation.components.AboutAppContent
import app.what.schedule.features.settings.presentation.components.asInstitutionChoice
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Clear
import app.what.schedule.ui.theme.icons.filled.Code
import app.what.schedule.ui.theme.icons.filled.Crown
import app.what.schedule.ui.theme.icons.filled.ImageRoller
import app.what.schedule.utils.AppUtils
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.context.unloadKoinModules

// Внутреннее состояние для под-экрана
private data class SubScreenState(
    val title: String,
    val description: String,
    val content: List<UIComponent>
)

@Composable
fun SettingsView(
    state: SettingsState,
    listener: Listener<SettingsEvent>
) {
    val appValues: AppValues = koinInject()
    val appUtils: AppUtils = koinInject()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 2 }

    var subScreen by useState<SubScreenState?>(null)

    val navigateToSubScreen: (String, String, List<UIComponent>) -> Unit = { title, desc, list ->
        subScreen = SubScreenState(title, desc, list)
        scope.launch { pagerState.animateScrollToPage(1) }
    }

    BackHandler(pagerState.currentPage != 0) {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    CompositionLocalProvider(LocalSettingsNavigator provides navigateToSubScreen) {
        val rootComponents = getSettingsList(appValues, appUtils)

        Column(Modifier.fillMaxSize()) {
            val headerTitle = if (pagerState.currentPage == 0) "Настройки"
            else subScreen?.title ?: ""

            val headerDesc = if (pagerState.currentPage == 0) listOf(
                "( ˶°ㅁ°) !!",
                "(๑ᵔ⤙ᵔ๑)",
                "(˶ˆᗜˆ˵)",
                "◝(ᵔᗜᵔ)◜",
                "⸜(｡˃ ᵕ ˂ )⸝♡",
                "(๑>◡<๑)",
                "(˶˃⤙˂˶)"
            ).random() else subScreen?.description ?: ""

            SettingsHeader(
                title = headerTitle,
                description = headerDesc,
                showBack = pagerState.currentPage != 0,
                onBack = { scope.launch { pagerState.animateScrollToPage(0) } }
            )
            // --------------

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) { page ->
                LazyColumn(Modifier.fillMaxSize()) {
                    if (page == 0) {
                        // Главная страница
                        item { SettingUpdateComponent.content(Modifier) }

                        items(rootComponents.size) { index ->
                            rootComponents[index].content(Modifier)
                        }
                    } else {
                        // Вторая страница
                        val components = subScreen?.content ?: emptyList()
                        items(components.size) { index ->
                            components[index].content(Modifier)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(
    title: String,
    description: String,
    showBack: Boolean,
    onBack: () -> Unit
) = Box(
    Modifier
        .animateContentSize()
        .height(if (keyboardAsState().value) 20.dp else 240.dp)
) {
    if (showBack) IconButton(modifier = Modifier.padding(start = 8.dp), onClick = onBack) {
        WHATIcons.Clear.Show(Modifier.size(24.dp), colorScheme.primary)
    }

    Column(
        Modifier.align(Alignment.BottomStart)
    ) {
        AnimatedEnter {
            Text(
                text = title,
                style = typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 46.sp,
                color = colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 18.dp, end = 16.dp)
            )
        }

        AnimatedEnter(delay = 200) {
            Text(
                text = description,
                style = typography.titleLarge,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Monospace,
                color = colorScheme.secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 16.dp)
            )
        }
    }
}


@Composable
fun getSettingsList(app: AppValues, utils: AppUtils): List<UIComponent> {
    val dialog = rememberDialogController()

    return listOf(
        category(
            "Основные", "базовые параметры", WHATIcons.Crown,
            content = listOf(
                app.institution.asInstitutionChoice {
                    app.lastSearch.set(null)
                    unloadKoinModules(controllers)
                    utils.restart()
                }
            )
        ),

        category(
            "Внешний вид", "тема, цвета, анимации", WHATIcons.ImageRoller,
            content = listOf(
                app.themeType.asSingleChoice(enumValues<ThemeType>()) { it.displayName },
                app.themeStyle.asSingleChoice(enumValues<ThemeStyle>()) { it.displayName },
                app.themeColor.asColorPalette()
                    .dependsOn(app.themeStyle) { it == ThemeStyle.CustomColor },
                app.useAnimation.asSwitch()
            )
        ),

        category(
            "Для разработчиков", "отладка", WHATIcons.Code,
            content = listOf(
                app.devSettingsUnlocked.asSwitch(),
                app.isFirstLaunch.asSwitch(),
                app.devPanelEnabled.asSwitch(),
                app.debugMode.asSwitch()
            )
        ).dependsOn(app.devSettingsUnlocked) { it == true },

        actionCategory(
            "О приложении", "версия, авторы", Icons.Rounded.Info
        ) {
            dialog.open { AboutAppContent(app, Modifier.fillMaxSize()) }
        }
    )
}

val LocalSettingsNavigator = staticCompositionLocalOf<(String, String, List<UIComponent>) -> Unit> {
    error("Settings navigator not provided")
}

fun category(
    title: String,
    description: String,
    icon: ImageVector,
    content: List<UIComponent>
) = object : UIComponent {
    @Composable
    override fun content(modifier: Modifier) {
        val navigate = LocalSettingsNavigator.current

        CategoryItem(
            icon = icon,
            title = title,
            description = description,
            onClick = { navigate(title, description, content) } // <-- Магия здесь
        )
    }
}

fun actionCategory(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) = object : UIComponent {
    @Composable
    override fun content(modifier: Modifier) {
        CategoryItem(icon, title, description, onClick = onClick)
    }
}

@Composable
fun CategoryItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) = Box(
    Modifier
        .clip(shapes.medium)
        .bclick(block = onClick)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(28.dp, 12.dp)
    ) {
        icon.Show(Modifier.size(28.dp), colorScheme.primary)

        Gap(18)

        Column {
            Text(title, style = typography.titleLarge, color = colorScheme.onBackground)
            Text(
                description,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = colorScheme.secondary
            )
        }
    }
}
package app.what.schedule.features.settings.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.core.Listener
import app.what.foundation.core.UIComponent
import app.what.foundation.data.settings.PreferenceStorage
import app.what.foundation.data.settings.Setting
import app.what.foundation.data.settings.types.BooleanSetting
import app.what.foundation.data.settings.types.EnumSettingView
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.animations.AnimatedEnter
import app.what.foundation.ui.bclick
import app.what.foundation.ui.capplyIf
import app.what.foundation.ui.keyboardAsState
import app.what.foundation.ui.theme.LocalThemeIsDark
import app.what.foundation.ui.useState
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.data.local.settings.ThemeStyle
import app.what.schedule.features.settings.domain.models.SettingsEvent
import app.what.schedule.features.settings.domain.models.SettingsState
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Clear
import app.what.schedule.ui.theme.icons.filled.Code
import app.what.schedule.ui.theme.icons.filled.Features
import app.what.schedule.ui.theme.icons.filled.Terminal
import com.materialkolor.ktx.DynamicScheme
import com.materialkolor.toColorScheme
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Composable
fun SettingsView(
    state: SettingsState,
    listener: Listener<SettingsEvent>
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxSize()
) {
    var selectedSettingCategory: SettingsCategory? by useState(null)
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 2 }
    val keyboardState by keyboardAsState()
    val updateComponent = remember { SettingUpdateComponent }

    BackHandler(pagerState.currentPage != 0) {
        scope.launch {
            selectedSettingCategory = null
            pagerState.animateScrollToPage(0)
        }
    }

    Box(
        Modifier
            .animateContentSize()
            .height(if (keyboardState) 20.dp else 240.dp)
    ) {
        if (pagerState.currentPage != 0) IconButton(
            onClick = {
                scope.launch {
                    selectedSettingCategory = null
                    pagerState.animateScrollToPage(0)
                }
            }
        ) {
            WHATIcons.Clear.Show(Modifier.size(24.dp), colorScheme.primary)
        }

        Column(
            Modifier.align(Alignment.BottomStart)
        ) {
            AnimatedEnter {
                Text(
                    text = selectedSettingCategory?.title ?: "Настройки",
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
                    text = selectedSettingCategory?.description
                        ?: "Настройте приложение так, как вам удобно",
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

    Gap(16)

    HorizontalPager(
        pagerState,
        key = { it },
        verticalAlignment = Alignment.Top,
        userScrollEnabled = false
    ) {
        Column {
            when (it) {
                0 -> {
                    updateComponent.content(Modifier)
                    SettingsCategory.entries.forEach {
                        CategoryItem(it.icon, it.title, it.description) {
                            scope.launch {
                                selectedSettingCategory = it
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    }
                }


                1 -> {
                    selectedSettingCategory ?: return@HorizontalPager

                    selectedSettingCategory!!.settings.forEach {
                        it.content(Modifier)
                    }
                }
            }
        }
    }
}

object SettingsViewConstructor : KoinComponent {
    private val appValues: AppValues by inject()

    val themeTypeSetting = Setting(
        icon = WHATIcons.Features,
        title = "Тема",
        description = "Выберите тему приложения",
        value = appValues.themeType
    ) { m, s -> EnumSettingView(m, s) }

    val themeStyleSetting = Setting(
        icon = WHATIcons.Features,
        title = "Стиль",
        description = "Стиль темы",
        value = appValues.themeStyle
    ) { m, s -> ThemeStyleSettingView(m, s, appValues.themeColor) }

    val devFeaturesSetting = Setting(
        icon = WHATIcons.Terminal,
        title = "Developer Mode",
        description = "Enable debugging tools, experimental features and advanced settings",
        value = appValues.devFeaturesEnabled
    ) { m, s -> BooleanSetting(m, s) }
}

@Composable
fun ThemeStyleSettingView(
    modifier: Modifier,
    setting: Setting<ThemeStyle>,
    themeColorSetting: PreferenceStorage.Value<ULong>,
) = Column {
    val value by setting.value.collect()
    val colors = remember {
        listOf(
            0xFFA1FF00,
            0xFFFF0600,
            0xFF586BFF,
            0xFFFF68A0,
            0xFFDAEDFF,
            0xFFFFFCF0,
            0xFFFFECBE,
            0xFFFFEEE0,
            0xFF24FFEA
        ).map { Color(it) }
    }

    EnumSettingView(Modifier, setting)

    AnimatedEnter(value == ThemeStyle.CustomColor) {
        val themeColor by themeColorSetting.collect { it?.let { Color(it) } }
        Gap(8)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Gap(24)
            colors.forEach {
                PaletteItem(
                    it, themeColor?.value == it.value
                ) { themeColorSetting.set(it.value) }
            }
            Gap(24)
        }
    }
}

@Composable
fun PaletteItem(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    MaterialTheme(
        colorScheme = DynamicScheme(color, LocalThemeIsDark.current).toColorScheme()
    ) {
        Box(
            Modifier
                .clip(shapes.medium)
                .background(colorScheme.surfaceContainer)
                .capplyIf(selected) { border(2.dp, colorScheme.primary, shapes.medium) }
                .padding(8.dp)
                .bclick(!selected, onClick)
        ) {
            Column {
                Spacer(
                    modifier = Modifier
                        .size(52.dp, 24.dp)
                        .clip(RoundedCornerShape(100.dp, 100.dp, 12.dp, 12.dp))
                        .background(colorScheme.primary)
                )

                Gap(4)

                Row {
                    Spacer(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp, 4.dp, 4.dp, 100.dp))
                            .background(colorScheme.secondary)
                    )

                    Gap(4)

                    Spacer(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp, 12.dp, 100.dp, 4.dp))
                            .background(colorScheme.tertiary)
                    )
                }
            }
        }
    }
}

enum class SettingsCategory(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val settings: List<UIComponent>
) {
    APPEARANCE(
        WHATIcons.Features,
        "Внешний вид",
        "тема, цвета",
        listOf(
            SettingsViewConstructor.themeTypeSetting,
            SettingsViewConstructor.themeStyleSetting,
        )
    ),
    DEV(
        WHATIcons.Code,
        "Для разработчиков",
        "мониторинг, логирование, отладка",
        listOf(
            SettingsViewConstructor.devFeaturesSetting
        )
    );
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
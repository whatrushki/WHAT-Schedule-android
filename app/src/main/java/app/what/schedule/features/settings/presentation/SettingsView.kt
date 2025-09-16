package app.what.schedule.features.settings.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.core.Listener
import app.what.foundation.core.UIComponent
import app.what.foundation.data.PreferenceStorage
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.bclick
import app.what.foundation.ui.keyboardAsState
import app.what.foundation.ui.useState
import app.what.navigation.core.rememberDialogController
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.features.settings.domain.models.SettingsEvent
import app.what.schedule.features.settings.domain.models.SettingsState
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Clear
import app.what.schedule.ui.theme.icons.filled.Code
import app.what.schedule.ui.theme.icons.filled.Features
import app.what.schedule.ui.theme.icons.filled.Terminal
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
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

    Box(
        Modifier
            .animateContentSize()
            .height(if (keyboardState) 20.dp else 120.dp)
    ) {
        IconButton(
            onClick = {
                scope.launch {
                    selectedSettingCategory = null
                    pagerState.animateScrollToPage(0)
                }
            }
        ) {
            WHATIcons.Clear.Show(Modifier.size(24.dp), colorScheme.primary)
        }
    }

    AnimatedContent(
        targetState = selectedSettingCategory,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        label = "TitleAnimation"
    ) { category ->
        Text(
            text = category?.title ?: "Настройки",
            style = typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 46.sp,
            color = colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 18.dp, end = 16.dp)
        )
    }

    AnimatedContent(
        targetState = selectedSettingCategory,
        transitionSpec = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(400)) togetherWith
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = tween(400))
        },
        label = "DescriptionAnimation"
    ) { category ->
        Text(
            text = category?.description ?: "",
            style = typography.titleLarge,
            fontStyle = FontStyle.Italic,
            fontFamily = FontFamily.Monospace,
            color = colorScheme.secondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 16.dp)
        )
    }

    Gap(16)

    HorizontalPager(
        pagerState,
        userScrollEnabled = false
    ) {
        Column {
            when (it) {
                0 -> SettingsCategory.entries.forEach {
                    CategoryItem(it.icon, it.title, it.description) {
                        scope.launch {
                            selectedSettingCategory = it
                            pagerState.animateScrollToPage(1)
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

    val themeSetting = Setting.EnumSetting(
        icon = WHATIcons.Features,
        title = "Тема",
        description = "Выберите тему приложения",
        options = enumValues<ThemeType>(),
        setting = appValues.themeType
    )

    val devFeaturesSetting = Setting.BooleanSetting(
        icon = WHATIcons.Terminal,
        title = "Developer Mode",
        description = "Enable debugging tools, experimental features and advanced settings",
        setting = appValues.devFeaturesEnabled
    )
}

enum class SettingsCategory(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val settings: List<Setting<*>>
) : KoinComponent {

    APPEARANCE(
        WHATIcons.Features,
        "Внешний вид",
        "тема, цвета",
        listOf(
            SettingsViewConstructor.themeSetting
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

sealed interface Setting<T : Any> : UIComponent {
    val icon: ImageVector
    val title: String
    val description: String
    val setting: PreferenceStorage.Value<T>

    class EnumSetting<T>(
        override val icon: ImageVector,
        override val title: String,
        override val description: String,
        val options: Array<T>,
        override val setting: PreferenceStorage.Value<T>,
        private val onValueChange: ((T) -> Unit)? = null
    ) : Setting<T> where T : Enum<T>, T : Named {
        @Composable
        override fun content(modifier: Modifier) {
            val currentValue by setting.collect() // Используем collect() из PreferenceStorage
            val dialogController = rememberDialogController()

            BaseSettingItem(
                icon = icon,
                title = title,
                description = description,
                supportingContent = {
                    EnumSelectionDialog(
                        currentValue = currentValue,
                        options = options,
                        onSelectionChange = { newValue ->
                            setting.set(newValue)
                            onValueChange?.invoke(newValue)
                            dialogController.close()
                        },
                        onDismiss = dialogController::close
                    )
                }
            )
        }
    }

    class BooleanSetting(
        override val icon: ImageVector,
        override val title: String,
        override val description: String,
        override val setting: PreferenceStorage.Value<Boolean>,
        private val onValueChange: ((Boolean) -> Unit)? = null
    ) : Setting<Boolean> {

        @Composable
        override fun content(modifier: Modifier) {
            val currentValue by setting.collect()
            val checked = currentValue ?: false

            BaseSettingItem(
                icon = icon,
                title = title,
                description = description,
                trailing = {
                    Switch(
                        checked = checked,
                        onCheckedChange = { newValue ->
                            setting.set(newValue)
                            onValueChange?.invoke(newValue)
                        }
                    )
                },
                supportingContent = null // Для boolean не нужен диалог
            )
        }
    }

    // Версия с валидацией
    class ValidatedStringSetting(
        override val icon: ImageVector,
        override val title: String,
        override val description: String,
        override val setting: PreferenceStorage.Value<String>,
        private val validationRules: ValidationRules = ValidationRules(),
        private val dialogTitle: String = "Редактировать",
        private val hint: String = "Введите значение",
        private val onValueChange: (String) -> Unit = {}
    ) : Setting<String> {
        data class ValidationRules(
            val required: Boolean = false,
            val minLength: Int? = null,
            val maxLength: Int? = null,
            val pattern: Regex? = null,
            val patternError: String? = null
        )

        fun validate(value: String): String? {
            return when {
                validationRules.required && value.isBlank() -> "Поле обязательно для заполнения"
                validationRules.minLength != null && value.length < validationRules.minLength ->
                    "Минимум ${validationRules.minLength} символов"

                validationRules.maxLength != null && value.length > validationRules.maxLength ->
                    "Максимум ${validationRules.maxLength} символов"

                validationRules.pattern != null && !validationRules.pattern.matches(value) ->
                    validationRules.patternError ?: "Неверный формат"

                else -> null
            }
        }

        @Composable
        override fun content(modifier: Modifier) {
            val currentValue by setting.collect()
            val dialogController = rememberDialogController()
            var tempValue by useState(currentValue ?: "")
            var error by useState<String?>(null)

            BaseSettingItem(
                icon = icon,
                title = title,
                description = description,
                trailing = {
                    Text(
                        text = currentValue ?: "Не задано",
                        style = typography.bodyMedium,
                        color = if (currentValue.isNullOrEmpty()) colorScheme.error
                        else colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    StringEditWithValidation(
                        title = dialogTitle,
                        currentValue = tempValue,
                        hint = hint,
                        error = error,
                        onValueChange = {
                            tempValue = it
                            error = validate(it)
                        },
                        onSave = {
                            if (error == null) {
                                setting.set(tempValue)
                                onValueChange(tempValue)
                                dialogController.close()
                            }
                        },
                        onDismiss = {
                            tempValue = currentValue ?: ""
                            error = null
                            dialogController.close()
                        }
                    )
                }
            )
        }
    }

    class StringSetting(
        override val icon: ImageVector,
        override val title: String,
        override val description: String,
        override val setting: PreferenceStorage.Value<String>,
        private val dialogTitle: String = "Редактировать",
        private val hint: String = "Введите значение",
        private val onValueChange: (String) -> Unit = {}
    ) : Setting<String> {

        @Composable
        override fun content(modifier: Modifier) {
            val currentValue by setting.collect()
            val dialogController = rememberDialogController()
            var tempValue by useState(currentValue ?: "")

            BaseSettingItem(
                icon = icon,
                title = title,
                description = description,
                trailing = {
                    Text(
                        text = currentValue ?: "Не задано",
                        style = typography.bodyMedium,
                        color = colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    StringEditDialog(
                        title = dialogTitle,
                        currentValue = tempValue,
                        hint = hint,
                        onValueChange = { newValue -> tempValue = newValue },
                        onSave = {
                            setting.set(tempValue)
                            onValueChange(tempValue)
                            dialogController.close()
                        },
                        onDismiss = {
                            tempValue = currentValue ?: ""
                            dialogController.close()
                        }
                    )
                }
            )

            // Сбрасываем временное значение при открытии диалога
            LaunchedEffect(dialogController.opened) {
                if (dialogController.opened) {
                    tempValue = currentValue ?: ""
                }
            }
        }
    }

    class CustomSetting<T : Any>(
        override val icon: ImageVector,
        override val title: String,
        override val description: String,
        override val setting: PreferenceStorage.Value<T>,
        private val onValueChange: (T) -> Unit = {},
        private val content: @Composable (T?, (T) -> Unit) -> Unit,
    ) : Setting<T> {

        @Composable
        override fun content(modifier: Modifier) {
            val value by setting.collect()
            content(value) {
                setting.set(it)
                onValueChange(it)
            }
        }
    }
}

@Composable
private fun StringEditDialog(
    title: String,
    currentValue: String,
    hint: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) = Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    Text(
        text = title,
        style = typography.headlineSmall,
        color = colorScheme.primary
    )

    OutlinedTextField(
        value = currentValue,
        onValueChange = onValueChange,
        label = { Text(hint) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onSave() }
        )
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onDismiss) {
            Text("Отмена")
        }
        Gap(8)
        Button(onClick = onSave) {
            Text("Сохранить")
        }
    }
}

@Composable
private fun StringEditWithValidation(
    title: String,
    currentValue: String,
    hint: String,
    error: String?,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    maxLength: Int? = null
) = Column(
    modifier = Modifier.padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    Text(
        text = title,
        style = typography.headlineSmall,
        color = colorScheme.primary
    )

    Column {
        OutlinedTextField(
            value = currentValue,
            onValueChange = { newValue ->
                if (maxLength == null || newValue.length <= maxLength) {
                    onValueChange(newValue)
                }
            },
            label = { Text(hint) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = error != null,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (error == null) onSave() }
            )
        )

        if (error != null) {
            Text(
                text = error,
                color = colorScheme.error,
                style = typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        maxLength?.let {
            Text(
                text = "${currentValue.length}/$maxLength",
                color = if (currentValue.length > maxLength) colorScheme.error
                else colorScheme.secondary,
                style = typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onDismiss) {
            Text("Отмена")
        }
        Gap(8)
        Button(
            onClick = onSave,
            enabled = error == null
        ) {
            Text("Сохранить")
        }
    }
}


@Composable
private fun <T> EnumSelectionDialog(
    currentValue: T?,
    options: Array<T>,
    onSelectionChange: (T) -> Unit,
    onDismiss: () -> Unit
) where T : Enum<T>, T : Named = Column {
    Text(
        text = "Выберите вариант",
        style = typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp)
    )

    options.forEach { option ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .bclick { onSelectionChange(option) }
                .padding(8.dp, 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = currentValue == option,
                onClick = { onSelectionChange(option) }
            )

            Gap(16)

            Text(
                text = option.displayName,
                style = typography.bodyLarge
            )
        }
    }
}

@Serializable
enum class ThemeType(override val displayName: String) : Named {
    Light("Светлая"),
    Dark("Тёмная"),
    System("Системная")
}

interface Named {
    val displayName: String
}

@Composable
fun BaseSettingItem(
    icon: ImageVector,
    title: String,
    description: String,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)?
) {
    val dialogController = rememberDialogController()

    Box(
        Modifier
            .clip(shapes.medium)
            .bclick(supportingContent != null && enabled) {
                dialogController.open(content = supportingContent!!)
            }
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

            trailing ?: return

            Gap(16)
            trailing()
        }
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
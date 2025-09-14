package app.what.foundation.data

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

typealias Projector<T> = @Composable (modifier: Modifier, metadata: UIMetadata, value: T?, setValue: (T?) -> Unit) -> Unit

data class UIMetadata(
    val icon: ImageVector? = null,
    val name: String = "",
    val description: String = "",
    val group: String = "" // Новое поле для группировки настроек
) {
    companion object {
        fun empty() = UIMetadata(null, "", "", "")
    }
}

// ========== SETTINGS NODES ==========

sealed class SettingsNode(open val metadata: UIMetadata, open val expand: Boolean) {
    data class Category(
        override val metadata: UIMetadata,
        override val expand: Boolean = false,
        val groups: List<SettingsGroup>
    ) : SettingsNode(metadata, expand)

    data class SettingsGroup(
        override val metadata: UIMetadata,
        override val expand: Boolean = false,
        val settings: List<Setting<*>>
    ) : SettingsNode(metadata, expand)

    data class Setting<T : Any>(
        override val metadata: UIMetadata,
        override val expand: Boolean = true,
        val value: PreferenceStorage.Value<T>,
        val projector: Projector<T>,
        val requiresDetailPage: Boolean = false // Флаг для настройки, требующей отдельной страницы
    ) : SettingsNode(metadata, expand) {

        @Composable
        fun Display(modifier: Modifier = Modifier, onNavigate: (SettingsNode) -> Unit = {}) {
            val currentValue by value.collect()
            if (requiresDetailPage) {
                // Для настроек, требующих отдельной страницы, показываем карточку с переходом
                Card(
                    modifier = modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(this) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    projector(Modifier.padding(16.dp), metadata, currentValue, value::set)
                }
            } else {
                // Для простых настроек показываем прямо в списке
                projector(modifier, metadata, currentValue, value::set)
            }
        }
    }
}

// ========== NAVIGATION ==========

class SettingsNavigator(initialNode: SettingsNode) {
    private val _backStack = mutableStateListOf(initialNode)
    val backStack: SnapshotStateList<SettingsNode> = _backStack
    val current: SettingsNode get() = _backStack.last()

    private var _isNavigatingForward = true

    val isNavigatingForward: Boolean
        get() = _isNavigatingForward

    fun navigateTo(node: SettingsNode) {
        _isNavigatingForward = true
        _backStack.add(node)
    }

    fun navigateBack(): Boolean {
        return if (_backStack.size > 1) {
            _isNavigatingForward = false
            _backStack.removeLast()
            true
        } else {
            false
        }
    }

    fun popTo(index: Int) {
        if (index < 0 || index >= _backStack.size - 1) return
        _isNavigatingForward = false
        while (_backStack.size > index + 1) {
            _backStack.removeLast()
        }
    }

    fun breadcrumbs(): List<String> = _backStack.map { it.metadata.name }
}

// ========== UI COMPONENTS ==========

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenGenerator(
    navigator: SettingsNavigator,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Boolean = { navigator.navigateBack() }
) {
    BackHandler(enabled = navigator.backStack.size > 1) {
        onBackPressed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = navigator.current.metadata.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (navigator.current.metadata.description.isNotBlank() && navigator.backStack.size == 1) {
                            Text(
                                text = navigator.current.metadata.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (navigator.backStack.size > 1) {
                        IconButton(onClick = { onBackPressed() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .then(modifier),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (navigator.backStack.size > 1) {
                    Breadcrumbs(navigator = navigator)
                }
                AnimatedContent(
                    targetState = navigator.current,
                    transitionSpec = {
                        val direction = if (navigator.isNavigatingForward) {
                            AnimatedContentTransitionScope.SlideDirection.Start
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.End
                        }

                        (slideIntoContainer(
                            direction,
                            animationSpec = tween(400, easing = EaseInOutCubic)
                        ) + fadeIn(animationSpec = tween(200, delayMillis = 100)))
                            .togetherWith(
                                slideOutOfContainer(
                                    direction,
                                    animationSpec = tween(400, easing = EaseInOutCubic)
                                ) + fadeOut(animationSpec = tween(150))
                            )
                    },
                    label = "SettingsScreenAnimation"
                ) { targetState ->
                    when (targetState) {
                        is SettingsNode.Category -> CategoryNodeUI(targetState, navigator::navigateTo)
                        is SettingsNode.SettingsGroup -> GroupNodeUI(targetState, navigator::navigateTo)
                        is SettingsNode.Setting<*> -> SettingDetailUI(targetState)
                    }
                }
            }
        }
    }
}

@Composable
private fun Breadcrumbs(
    navigator: SettingsNavigator,
    modifier: Modifier = Modifier
) {
    val crumbs = navigator.breadcrumbs()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        crumbs.forEachIndexed { index, name ->
            if (index > 0) {
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            val isLast = index == crumbs.size - 1
            Text(
                text = name,
                style = if (isLast) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                fontWeight = if (isLast) FontWeight.Medium else FontWeight.Normal,
                color = if (isLast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                modifier = if (!isLast) Modifier
                    .clickable {
                        navigator.popTo(index)
                    }
                    .padding(horizontal = 4.dp) else Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun CategoryNodeUI(
    node: SettingsNode.Category,
    onNavigate: (SettingsNode) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(node.groups) { group ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onNavigate(group) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = group.metadata.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    supportingContent = {
                        if (group.metadata.description.isNotBlank()) {
                            Text(
                                text = group.metadata.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingContent = {
                        group.metadata.icon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun GroupNodeUI(
    node: SettingsNode.SettingsGroup,
    onNavigate: (SettingsNode) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(node.settings) { setting ->
            setting.Display(
                modifier = Modifier.fillMaxWidth(),
                onNavigate = onNavigate
            )

            if (setting != node.settings.last()) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun SettingDetailUI(node: SettingsNode.Setting<*>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок настройки
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            node.metadata.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                text = node.metadata.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Описание
        if (node.metadata.description.isNotBlank()) {
            Text(
                text = node.metadata.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // Сама настройка
        node.Display(Modifier.fillMaxWidth())
    }
}

@Composable
fun BooleanSetting(
    value: Boolean?,
    onValueChange: (Boolean?) -> Unit,
    metadata: UIMetadata,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    ListItem(
        headlineContent = {
            Text(
                text = metadata.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            if (metadata.description.isNotBlank()) {
                Text(
                    text = metadata.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            metadata.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        trailingContent = {
            Switch(
                checked = value ?: false,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onValueChange(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        },
        modifier = modifier,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

// Пример создания структуры настроек

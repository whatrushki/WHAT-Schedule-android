package app.what.schedule.features.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import app.what.foundation.core.Listener
import app.what.foundation.data.BooleanSetting
import app.what.foundation.data.SettingsNavigator
import app.what.foundation.data.SettingsNode
import app.what.foundation.data.SettingsScreenGenerator
import app.what.foundation.data.UIMetadata
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.features.settings.domain.models.SettingsEvent
import app.what.schedule.features.settings.domain.models.SettingsState
import org.koin.compose.koinInject

@Composable
fun SettingsView(
    state: SettingsState,
    listener: Listener<SettingsEvent>
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.fillMaxSize()
) {
    val context = LocalContext.current
    val settings = koinInject<AppValues>()
    val settingsStructure = remember {
        SettingsNode.Category(
            metadata = UIMetadata(
                icon = Icons.Default.Settings,
                name = "Настройки",
                description = "Управление настройками приложения"
            ),
            groups = listOf(
                SettingsNode.SettingsGroup(
                    metadata = UIMetadata(
                        icon = Icons.Default.ThumbUp,
                        name = "Основные",
                        description = "Основные настройки приложения"
                    ),
                    settings = listOf(
                        SettingsNode.Setting(
                            metadata = UIMetadata(
                                icon = Icons.Default.ThumbUp,
                                name = "Уведомления",
                                description = "Управление уведомлениями",
                                group = "Основные"
                            ),
                            value = settings.isFirstLaunch,
                            projector = { modifier, metadata, value, setValue ->
                                BooleanSetting(
                                    value = value,
                                    onValueChange = setValue,
                                    metadata = metadata,
                                    modifier = modifier
                                )
                            }
                        ),
                        SettingsNode.Setting(
                            metadata = UIMetadata(
                                icon = Icons.Default.ThumbUp,
                                name = "Конфиденциальность",
                                description = "Настройки конфиденциальности",
                                group = "Основные"
                            ),
                            value = settings.isFirstLaunch,
                            projector = { modifier, metadata, value, setValue ->
                                BooleanSetting(
                                    value = value,
                                    onValueChange = setValue,
                                    metadata = metadata,
                                    modifier = modifier
                                )
                            },
                            requiresDetailPage = true
                        )
                    )
                ),
                SettingsNode.SettingsGroup(
                    metadata = UIMetadata(
                        icon = Icons.Default.ThumbUp,
                        name = "Оформление",
                        description = "Настройки внешнего вида"
                    ),
                    settings = listOf(
                        SettingsNode.Setting(
                            metadata = UIMetadata(
                                icon = Icons.Default.ThumbUp,
                                name = "Темная тема",
                                description = "Включение темной темы",
                                group = "Оформление"
                            ),
                            value = settings.isFirstLaunch,
                            projector = { modifier, metadata, value, setValue ->
                                BooleanSetting(
                                    value = value,
                                    onValueChange = setValue,
                                    metadata = metadata,
                                    modifier = modifier
                                )
                            }
                        )
                    )
                ),
                SettingsNode.SettingsGroup(
                    metadata = UIMetadata(
                        icon = Icons.Default.Info,
                        name = "О приложении",
                        description = "Информация о приложении"
                    ),
                    settings = listOf(
                        SettingsNode.Setting(
                            metadata = UIMetadata(
                                icon = Icons.Default.Info,
                                name = "Версия",
                                description = "Версия приложения",
                                group = "О приложении"
                            ),
                            value = settings.isFirstLaunch,
                            projector = { modifier, metadata, value, setValue ->
                                // Просто отображаем информацию
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = metadata.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            text = "1.0.0",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    modifier = modifier
                                )
                            },
                            requiresDetailPage = true
                        )
                    )
                )
            )
        )
    }

    val navigator = remember { SettingsNavigator(settingsStructure) }

    SettingsScreenGenerator(navigator = navigator)
}
package app.what.schedule.features.dev.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.useState
import app.what.schedule.ui.components.Fallback
import app.what.schedule.ui.components.SearchTextField
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Clear
import app.what.schedule.ui.theme.icons.filled.Export
import app.what.schedule.ui.theme.icons.filled.Pause
import app.what.schedule.ui.theme.icons.filled.Question
import app.what.schedule.ui.theme.icons.filled.Resume

interface Filter<T> {
    fun clearFilters()
    fun parseQuery(query: String)
    fun matches(value: T): Boolean
}

fun <T> List<T>.applyFilters(filter: Filter<T>, query: String): List<T> {
    if (query.isBlank()) return this

    return try {
        filter.parseQuery(query)
        this.filter { filter.matches(it) }
    } catch (e: Exception) {
        // В случае ошибки парсинга возвращаем все логи
        this
    }
}

@Composable
fun <T> FilteredList(
    title: String,
    values: List<T>,
    vKey: (T) -> Any,
    vContent: @Composable (T) -> Unit,
    exportValues: () -> Unit,
    clearValues: () -> Unit,
    setIsMonitoringPaused: (Boolean) -> Unit,
    isMonitoringPaused: Boolean,
    filter: Filter<T>,
    filterHelpItems: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    val (filterText, setFilterText) = useState("")
    var isHelpDialogExpanded by remember { mutableStateOf(false) }
    val filteredValues = remember(values, filterText) {
        values.applyFilters(filter, filterText)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок и управление
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )

            // Индикатор активности
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Счетчик логов
                Text(
                    text = "${filteredValues.size}/${values.size}",
                    style = typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )

                Gap(8)

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (isMonitoringPaused) colorScheme.error
                            else colorScheme.primary,
                            CircleShape
                        )
                )
                Text(
                    text = if (isMonitoringPaused) "Пауза" else "Запись",
                    style = typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        // Панель управления и фильтрации
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchTextField(
                value = filterText,
                onValueChange = setFilterText,
                modifier = Modifier
                    .weight(1f)
                    .focusable(false),
                placeholder = "фильтр",
                debounce = 500,
                trailing = {
                    if (filterText.isNotEmpty()) {
                        IconButton(
                            onClick = { setFilterText("") },
                        ) {
                            Icons.Default.Close.Show(color = colorScheme.onSurface)
                        }
                    } else {
                        IconButton(
                            onClick = { isHelpDialogExpanded = true }
                        ) {
                            WHATIcons.Question.Show(
                                Modifier
                                    .rotate(18f)
                                    .size(18.dp),
                                colorScheme.onSurface
                            )
                        }
                    }
                }
            )

            // Кнопка паузы/продолжения
            IconButton(
                onClick = {
                    setIsMonitoringPaused(!isMonitoringPaused)
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isMonitoringPaused) colorScheme.errorContainer
                        else colorScheme.primaryContainer,
                        CircleShape
                    )
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = if (isMonitoringPaused) WHATIcons.Resume else WHATIcons.Pause,
                    contentDescription = if (isMonitoringPaused) "Продолжить" else "Пауза",
                    tint = if (isMonitoringPaused) colorScheme.onErrorContainer
                    else colorScheme.onPrimaryContainer
                )
            }

            // Кнопка очистки
            IconButton(
                onClick = clearValues,
                modifier = Modifier
                    .size(48.dp)
                    .background(colorScheme.surfaceContainer, CircleShape)
            ) {
                WHATIcons.Clear.Show(
                    Modifier.size(18.dp),
                    colorScheme.onSurface
                )
            }

            // Кнопка экспорта
            IconButton(
                onClick = exportValues,
                modifier = Modifier
                    .size(48.dp)
                    .background(colorScheme.surfaceContainer, CircleShape)
            ) {
                WHATIcons.Export.Show(
                    Modifier.size(18.dp),
                    colorScheme.onSurface
                )
            }
        }

        Gap(8)

        // Справка по фильтрам (expandable)
        if (isHelpDialogExpanded) {
            FilterHelpDialog(
                items = filterHelpItems,
                onDismiss = { isHelpDialogExpanded = false }
            )
        }

        // Список логов
        if (filteredValues.isEmpty()) {
            Fallback(
                text = if (values.isEmpty()) "Записи отсутствуют"
                else "Ничего не найдено по фильтру",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredValues.reversed(), key = vKey) {
                    vContent(it)
                }
            }
        }
    }
}


@Composable
fun FilterHelpDialog(
    items: List<Pair<String, String>>,
    onDismiss: () -> Unit
) = Dialog(
    onDismissRequest = onDismiss
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .clip(shapes.extraLarge)
                .background(colorScheme.surfaceContainer)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Синтаксис фильтров",
                style = typography.headlineMedium,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Gap(12)

            items.forEach {
                FilterHelpItem(
                    it.first,
                    it.second
                )
            }

            Gap(8)

            Text(
                "Можно комбинировать: ${items.take(3).joinToString { it.first }}",
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )

            Gap(8)

            TextButton(onClick = onDismiss) {
                Text(
                    text = "Понятно",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun FilterHelpItem(command: String, description: String) = Row(
    verticalAlignment = Alignment.Top
) {
    Text(
        text = command,
        style = typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        ),
        color = colorScheme.primary,
        modifier = Modifier.width(120.dp)
    )

    Gap(8)

    Text(
        text = description,
        style = typography.bodySmall,
        color = colorScheme.onSurfaceVariant,
        modifier = Modifier.weight(1f)
    )
}
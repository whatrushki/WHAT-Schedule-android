package app.what.schedule.features.insts.dgtu.presentation.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.core.Listener
import app.what.foundation.data.RemoteState
import app.what.foundation.ui.Gap
import app.what.foundation.ui.bclick
import app.what.schedule.features.insts.dgtu.domain.models.DgtuEvent
import app.what.schedule.features.insts.dgtu.domain.models.DgtuState
import app.what.schedule.features.insts.dgtu.domain.models.Mail
import app.what.schedule.ui.components.AsyncImageWithFallback
import app.what.schedule.ui.components.Fallback
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DgtuMailsPage(
    state: State<DgtuState>,
    listener: Listener<DgtuEvent>
) = PullToRefreshBox(
    isRefreshing = state.value.mailsFetchState == RemoteState.Loading,
    onRefresh = { listener(DgtuEvent.MailsOpened) },
) {
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState.canScrollForward) {
        if (!lazyListState.canScrollForward && state.value.mailsFetchState != RemoteState.Loading)
            listener(DgtuEvent.OnMailsListEndingScrolled)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState
    ) {
        when (state.value.mailsFetchState) {
            is RemoteState.Error -> item {
                Fallback(
                    "Произошла непредвиденная ошибка",
                    Modifier.fillMaxSize(),
                    "Попробовать снова" to { listener(DgtuEvent.MailsOpened) }
                )
            }

            RemoteState.Success, RemoteState.Loading -> items(state.value.mails, key = { it.id }) {
                MailListItem(it, Modifier.animateItem())
            }

            else -> Unit
        }
    }
}

@Composable
fun MailListItem(data: Mail, modifier: Modifier) = Box(
    modifier.bclick {

    }
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        AsyncImageWithFallback(
            data.photoLink,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
        )

        Gap(12)

        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    formatName(data.sender),
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    fontSize = 16.sp,
                )

            Text(
                formatDateTime(data.sendDateTime),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )
            }

            Text(
                data.title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colorScheme.secondary
            )

            Text(
                data.description,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatName(value: String) = value.split(" ").let {
    if (it.size == 3) "${it[0]} ${it[0][0]}. ${it[0][0]}."
    else value
}

fun formatDateTime(value: LocalDateTime): String {
    val today = LocalDate.now()
    val date = value.toLocalDate()

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    val shortMonthFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())

    return when {
        date == today -> value.format(timeFormatter)
        date == today.minusDays(1) -> "вчера"
        date == today.minusDays(2) -> "позавчера"
        date.year == today.year -> value.format(shortMonthFormatter)
            .replaceFirstChar { it.uppercaseChar() }
        else -> value.format(shortMonthFormatter)
            .replaceFirstChar { it.uppercaseChar() }
    }
}
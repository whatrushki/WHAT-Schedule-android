package app.what.foundation.services.crash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.rememberScrollable2DState
import androidx.compose.foundation.gestures.scrollable2D
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.what.foundation.ui.useState


@Composable
fun CrashScreen(
    crashReport: String,
    onRestart: () -> Unit,
    onShare: () -> Unit
) {
    var showDetails by useState(false)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Иконка ошибки
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Crash",
                tint = colorScheme.error,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Заголовок
            Text(
                text = "Приложение остановлено",
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Описание
            Text(
                text = "Произошла непредвиденная ошибка. Приносим извинения за неудобства.",
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопки
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Перезапустить приложение")
                }

                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Поделиться отчетом")
                }

                TextButton(
                    onClick = { showDetails = !showDetails }
                ) {
                    Text(if (showDetails) "Скрыть детали" else "Показать детали")
                }
            }

            // Детали ошибки
            if (showDetails) {
                Spacer(modifier = Modifier.height(16.dp))
                CrashDetails(crashReport = crashReport)
            }
        }
    }
}

@Composable
fun CrashDetails(crashReport: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = shapes.medium,
        color = colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            val vScrollState = rememberScrollState()
            val hScrollState = rememberScrollState()

            Text(
                text = crashReport,
                style = typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = colorScheme.onSurface,
                modifier = Modifier
                    .verticalScroll(vScrollState)
                    .horizontalScroll(hScrollState)
                    .fillMaxSize()
            )
        }
    }
}
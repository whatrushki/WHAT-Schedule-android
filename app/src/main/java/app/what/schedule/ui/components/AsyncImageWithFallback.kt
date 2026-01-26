package app.what.schedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.what.foundation.ui.Gap
import app.what.foundation.ui.applyIf
import app.what.foundation.ui.bclick
import app.what.foundation.ui.useState
import app.what.schedule.utils.DownloadUtils
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.ImageRequest
import coil3.request.crossfade
import java.time.LocalDateTime

@Composable
fun AsyncImageWithFallback(
    url: String?,
    modifier: Modifier = Modifier,
    enableDetailView: Boolean = false
) {
    var showFullScreen by useState(false)

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
    ) {
        val state by painter.state.collectAsState()

        when (state) {
            is AsyncImagePainter.State.Success -> {
                SubcomposeAsyncImageContent(
                    modifier = Modifier.applyIf(enableDetailView) {
                        bclick { showFullScreen = true }
                    }
                )
            }

            is AsyncImagePainter.State.Loading -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxSize())
            }

            else -> {
                // Можно добавить placeholder ошибки, например иконку
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Build, null, tint = colorScheme.primary)
                }
            }
        }

        if (showFullScreen) {
            FullScreenImageDialog(
                url = url,
                onDismiss = { showFullScreen = false }
            )
        }
    }
}

@Composable
private fun FullScreenImageDialog(
    url: String?,
    onDismiss: () -> Unit
) = Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
        usePlatformDefaultWidth = false,
        decorFitsSystemWindows = false
    )
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .bclick { onDismiss() }
    ) {
        ZoomableBox(
            modifier = Modifier
                .fillMaxSize()
                .bclick(enabled = false) { /* пустой обработчик */ }
        ) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                    }
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        }


        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .padding(horizontal = 12.dp)
                .systemBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .bclick {
                        url?.let {
                            DownloadUtils.downloadImage(
                                context,
                                it,
                                "schedule_new_image_${LocalDateTime.now()}.jpg"
                            )
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Загрузить",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(6.dp)
                )

                Text(
                    text = "Загрузить",
                    color = Color.White
                )

                Gap(8)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .bclick(block = onDismiss)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(6.dp)
                )
            }
        }
    }
}

@Composable
fun ZoomableBox(
    modifier: Modifier = Modifier,
    minScale: Float = 1f,
    maxScale: Float = 5f,
    content: @Composable ZoomableBoxScope.() -> Unit
) {
    var scale by useState(1f)
    var offsetX by useState(0f)
    var offsetY by useState(0f)
    var size by useState(IntSize.Zero)

    Box(
        modifier = modifier
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val oldScale = scale
                    scale = (scale * zoom).coerceIn(minScale, maxScale)

                    // Центрирование зума
                    val scaleRatio = scale / oldScale
                    offsetX = offsetX * scaleRatio + pan.x
//                    offsetY = offsetY * scaleRatio + pan.y

                    // Безопасное ограничение
                    val extraX = ((size.width * (scale - 1f)).coerceAtLeast(0f)) / 2f
                    val extraY = ((size.height * (scale - 1f)).coerceAtLeast(0f)) / 2f

                    offsetX = offsetX.coerceIn(-extraX, extraX)
//                    offsetY = offsetY.coerceIn(-extraY, extraY)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale == 1f) 2.5f else 1f
                        offsetY = 0f
                        offsetX = 0f
                    }
                )
            }
    ) {
        val scope = ZoomableBoxScopeImpl(scale, offsetX, offsetY)
        scope.content()
    }
}

interface ZoomableBoxScope {
    val scale: Float
    val offsetX: Float
    val offsetY: Float
}

private data class ZoomableBoxScopeImpl(
    override val scale: Float,
    override val offsetX: Float,
    override val offsetY: Float
) : ZoomableBoxScope
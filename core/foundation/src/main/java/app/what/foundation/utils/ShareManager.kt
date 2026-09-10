package app.what.foundation.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import app.what.foundation.ui.Gap
import app.what.foundation.ui.components.ShareButton
import app.what.foundation.ui.controllers.SheetController
import app.what.foundation.ui.controllers.rememberSheetController
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Copy
import app.what.schedule.ui.theme.icons.filled.Telegram
import app.what.schedule.ui.theme.icons.filled.VK
import app.what.schedule.ui.theme.icons.filled.Whatsapp

sealed interface ShareData {
    val title: String?

    data class Text(
        val text: String,
        override val title: String? = null
    ) : ShareData

    data class File(
        val uri: Uri,
        val mimeType: String = "*/*",
        override val title: String? = null,
        val text: String? = null
    ) : ShareData

    data class Files(
        val uris: List<Uri>,
        val mimeType: String = "*/*",
        override val title: String? = null,
        val text: String? = null
    ) : ShareData

    data class Image(
        val uri: Uri,
        val mimeType: String = "image/*",
        override val title: String? = null,
        val text: String? = null
    ) : ShareData

    data class Images(
        val uris: List<Uri>,
        val mimeType: String = "image/*",
        override val title: String? = null,
        val text: String? = null
    ) : ShareData

    companion object {
        fun text(text: String, title: String? = null): ShareData = Text(text, title)
        fun file(uri: Uri, mimeType: String = "*/*", title: String? = null, text: String? = null): ShareData =
            File(uri, mimeType, title, text)
        fun files(uris: List<Uri>, mimeType: String = "*/*", title: String? = null, text: String? = null): ShareData =
            Files(uris, mimeType, title, text)
        fun image(uri: Uri, title: String? = null, text: String? = null): ShareData =
            Image(uri, "image/*", title, text)
    }
}

enum class ShareChannel {
    Telegram,
    VK,
    WhatsApp,
    Clipboard,
    SystemDefault;

    companion object {
        val defaultChannels: List<ShareChannel> = listOf(
            SystemDefault,
            Telegram,
            VK,
            WhatsApp,
            Clipboard
        )
    }
}

class ShareManager(
    val sheetController: SheetController,
    val context: Context
) {
    fun share(
        data: ShareData,
        channels: List<ShareChannel> = ShareChannel.defaultChannels
    ) {
        sheetController.open(full = false) {
            ShareManagerPane(
                data = data,
                channels = channels,
                onShare = { channel ->
                    execute(data, channel)
                    sheetController.animateClose()
                }
            )
        }
    }

    fun share(
        text: String,
        title: String? = null,
        channels: List<ShareChannel> = ShareChannel.defaultChannels
    ) = share(ShareData.Text(text, title), channels)

    fun <T> shareCustom(
        title: String = "Поделиться",
        items: List<T>,
        initialSelection: List<T> = items,
        channels: List<ShareChannel> = ShareChannel.defaultChannels,
        itemLabel: (T) -> String,
        onResult: (List<T>) -> ShareData
    ) {
        sheetController.open(full = false) {
            CustomSharePane(
                title = title,
                items = items,
                initialSelection = initialSelection,
                channels = channels,
                itemLabel = itemLabel,
                onShare = { channel, selectedItems ->
                    val data = onResult(selectedItems)
                    execute(data, channel)
                    sheetController.animateClose()
                }
            )
        }
    }

    fun execute(data: ShareData, channel: ShareChannel = ShareChannel.SystemDefault) {
        executeShare(context, channel, data)
    }
}

@Composable
fun rememberShareManager(
    sheetController: SheetController = rememberSheetController(),
    context: Context = LocalContext.current
): ShareManager = remember(sheetController, context) {
    ShareManager(sheetController, context)
}

fun executeShare(context: Context, channel: ShareChannel, data: ShareData) {
    when (channel) {
        ShareChannel.Clipboard -> {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            when (data) {
                is ShareData.Text -> {
                    val clip = ClipData.newPlainText(data.title ?: "Shared Text", data.text)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Скопировано в буфер обмена", Toast.LENGTH_SHORT).show()
                }
                is ShareData.File -> {
                    val clip = ClipData.newUri(context.contentResolver, data.title ?: "Shared File", data.uri)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Файл скопирован", Toast.LENGTH_SHORT).show()
                }
                is ShareData.Image -> {
                    val clip = ClipData.newUri(context.contentResolver, data.title ?: "Shared Image", data.uri)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Изображение скопировано", Toast.LENGTH_SHORT).show()
                }
                is ShareData.Files -> {
                    val text = data.uris.joinToString("\n") { it.toString() }
                    val clip = ClipData.newPlainText(data.title ?: "Shared Files", text)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Ссылки на файлы скопированы", Toast.LENGTH_SHORT).show()
                }
                is ShareData.Images -> {
                    val text = data.uris.joinToString("\n") { it.toString() }
                    val clip = ClipData.newPlainText(data.title ?: "Shared Images", text)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Ссылки на изображения скопированы", Toast.LENGTH_SHORT).show()
                }
            }
        }

        ShareChannel.SystemDefault -> {
            when (data) {
                is ShareData.Text -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, data.text)
                        data.title?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(intent, data.title ?: "Поделиться").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
                is ShareData.File -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = data.mimeType
                        putExtra(Intent.EXTRA_STREAM, data.uri)
                        data.text?.let { putExtra(Intent.EXTRA_TEXT, it) }
                        data.title?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(intent, data.title ?: "Поделиться").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
                is ShareData.Image -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = data.mimeType
                        putExtra(Intent.EXTRA_STREAM, data.uri)
                        data.text?.let { putExtra(Intent.EXTRA_TEXT, it) }
                        data.title?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(intent, data.title ?: "Поделиться").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
                is ShareData.Files -> {
                    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = data.mimeType
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(data.uris))
                        data.text?.let { putExtra(Intent.EXTRA_TEXT, it) }
                        data.title?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(intent, data.title ?: "Поделиться").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
                is ShareData.Images -> {
                    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = data.mimeType
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(data.uris))
                        data.text?.let { putExtra(Intent.EXTRA_TEXT, it) }
                        data.title?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(intent, data.title ?: "Поделиться").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            }
        }

        ShareChannel.Telegram -> {
            when (data) {
                is ShareData.Text -> {
                    val encoded = Uri.encode(data.text)
                    val deepLink = "tg://msg?text=$encoded".toUri()
                    val intent = Intent(Intent.ACTION_VIEW, deepLink).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching {
                        context.startActivity(intent)
                    }.onFailure {
                        val packageIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, data.text)
                            setPackage("org.telegram.messenger")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        runCatching {
                            context.startActivity(packageIntent)
                        }.onFailure {
                            executeShare(context, ShareChannel.SystemDefault, data)
                        }
                    }
                }
                is ShareData.File, is ShareData.Image -> {
                    val uri = if (data is ShareData.File) data.uri else (data as ShareData.Image).uri
                    val mime = if (data is ShareData.File) data.mimeType else (data as ShareData.Image).mimeType
                    val text = if (data is ShareData.File) data.text else (data as ShareData.Image).text
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = mime
                        putExtra(Intent.EXTRA_STREAM, uri)
                        text?.let { putExtra(Intent.EXTRA_TEXT, it) }
                        setPackage("org.telegram.messenger")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching {
                        context.startActivity(intent)
                    }.onFailure {
                        executeShare(context, ShareChannel.SystemDefault, data)
                    }
                }
                is ShareData.Files, is ShareData.Images -> {
                    val uris = if (data is ShareData.Files) data.uris else (data as ShareData.Images).uris
                    val mime = if (data is ShareData.Files) data.mimeType else (data as ShareData.Images).mimeType
                    val text = if (data is ShareData.Files) data.text else (data as ShareData.Images).text
                    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = mime
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                        text?.let { putExtra(Intent.EXTRA_TEXT, it) }
                        setPackage("org.telegram.messenger")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching {
                        context.startActivity(intent)
                    }.onFailure {
                        executeShare(context, ShareChannel.SystemDefault, data)
                    }
                }
            }
        }

        ShareChannel.VK -> {
            when (data) {
                is ShareData.Text -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, data.text)
                        setPackage("com.vkontakte.android")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching {
                        context.startActivity(intent)
                    }.onFailure {
                        executeShare(context, ShareChannel.SystemDefault, data)
                    }
                }
                is ShareData.File, is ShareData.Image -> {
                    val uri = if (data is ShareData.File) data.uri else (data as ShareData.Image).uri
                    val mime = if (data is ShareData.File) data.mimeType else (data as ShareData.Image).mimeType
                    val text = if (data is ShareData.File) data.text else (data as ShareData.Image).text
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = mime
                        putExtra(Intent.EXTRA_STREAM, uri)
                        text?.let { putExtra(Intent.EXTRA_TEXT, it) }
                        setPackage("com.vkontakte.android")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching {
                        context.startActivity(intent)
                    }.onFailure {
                        executeShare(context, ShareChannel.SystemDefault, data)
                    }
                }
                is ShareData.Files, is ShareData.Images -> {
                    val uris = if (data is ShareData.Files) data.uris else (data as ShareData.Images).uris
                    val mime = if (data is ShareData.Files) data.mimeType else (data as ShareData.Images).mimeType
                    val text = if (data is ShareData.Files) data.text else (data as ShareData.Images).text
                    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = mime
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                        text?.let { putExtra(Intent.EXTRA_TEXT, it) }
                        setPackage("com.vkontakte.android")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching {
                        context.startActivity(intent)
                    }.onFailure {
                        executeShare(context, ShareChannel.SystemDefault, data)
                    }
                }
            }
        }

        ShareChannel.WhatsApp -> {
            when (data) {
                is ShareData.Text -> {
                    val encoded = Uri.encode(data.text)
                    val deepLink = "https://api.whatsapp.com/send?text=$encoded".toUri()
                    val intent = Intent(Intent.ACTION_VIEW, deepLink).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching {
                        context.startActivity(intent)
                    }.onFailure {
                        val packageIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, data.text)
                            setPackage("com.whatsapp")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        runCatching {
                            context.startActivity(packageIntent)
                        }.onFailure {
                            executeShare(context, ShareChannel.SystemDefault, data)
                        }
                    }
                }
                is ShareData.File, is ShareData.Image -> {
                    val uri = if (data is ShareData.File) data.uri else (data as ShareData.Image).uri
                    val mime = if (data is ShareData.File) data.mimeType else (data as ShareData.Image).mimeType
                    val text = if (data is ShareData.File) data.text else (data as ShareData.Image).text
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = mime
                        putExtra(Intent.EXTRA_STREAM, uri)
                        text?.let { putExtra(Intent.EXTRA_TEXT, it) }
                        setPackage("com.whatsapp")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching {
                        context.startActivity(intent)
                    }.onFailure {
                        executeShare(context, ShareChannel.SystemDefault, data)
                    }
                }
                is ShareData.Files, is ShareData.Images -> {
                    val uris = if (data is ShareData.Files) data.uris else (data as ShareData.Images).uris
                    val mime = if (data is ShareData.Files) data.mimeType else (data as ShareData.Images).mimeType
                    val text = if (data is ShareData.Files) data.text else (data as ShareData.Images).text
                    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = mime
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                        text?.let { putExtra(Intent.EXTRA_TEXT, it) }
                        setPackage("com.whatsapp")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching {
                        context.startActivity(intent)
                    }.onFailure {
                        executeShare(context, ShareChannel.SystemDefault, data)
                    }
                }
            }
        }
    }
}

@Composable
fun ShareManagerPane(
    data: ShareData,
    channels: List<ShareChannel>,
    onShare: (ShareChannel) -> Unit
) {
    Column(
        Modifier
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            data.title ?: "Поделиться",
            style = typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary
        )

        Gap(12)

        when (data) {
            is ShareData.Text -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shapes.medium)
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text(
                        data.text,
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Gap(16)
            }
            is ShareData.File -> {
                Text(
                    "Файл: ${data.uri.lastPathSegment ?: "файл"}",
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Gap(16)
            }
            is ShareData.Files -> {
                Text(
                    "Файлов: ${data.uris.size}",
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Gap(16)
            }
            is ShareData.Image -> {
                Text(
                    "Изображение",
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Gap(16)
            }
            is ShareData.Images -> {
                Text(
                    "Изображений: ${data.uris.size}",
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Gap(16)
            }
        }

        Text("Куда отправить:", style = typography.labelLarge, color = colorScheme.onSurface)
        Gap(12)

        ShareChannelsRow(channels = channels, onChannelClick = onShare)

        Gap(12)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> CustomSharePane(
    title: String,
    items: List<T>,
    initialSelection: List<T>,
    channels: List<ShareChannel>,
    itemLabel: (T) -> String,
    onShare: (ShareChannel, List<T>) -> Unit
) {
    val selectedItems = remember { mutableStateListOf<T>().apply { addAll(initialSelection) } }

    Column(
        Modifier
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            title,
            style = typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary
        )

        Gap(16)

        Text("Выберите элементы:", style = typography.labelLarge)
        Gap(8)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                val selected = item in selectedItems
                FilterChip(
                    selected = selected,
                    onClick = {
                        if (selected) {
                            if (selectedItems.size > 1) selectedItems.remove(item)
                        } else {
                            selectedItems.add(item)
                        }
                    },
                    label = { Text(itemLabel(item)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorScheme.primary,
                        selectedLabelColor = colorScheme.onPrimary
                    )
                )
            }
        }

        Gap(24)

        Text("Куда отправить:", style = typography.labelLarge)
        Gap(12)

        ShareChannelsRow(channels = channels) { channel ->
            onShare(channel, selectedItems.toList())
        }

        Gap(12)
    }
}

@Composable
fun ShareChannelsRow(
    channels: List<ShareChannel>,
    onChannelClick: (ShareChannel) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        channels.forEach { channel ->
            when (channel) {
                ShareChannel.SystemDefault -> {
                    ShareButton(
                        icon = Icons.Default.MoreVert,
                        color = colorScheme.secondaryContainer,
                        background = colorScheme.onSecondaryContainer,
                        iconSize = 34,
                        onClick = { onChannelClick(channel) }
                    )
                }
                ShareChannel.Telegram -> {
                    ShareButton(
                        icon = WHATIcons.Telegram,
                        color = Color(0xFF2AABEE),
                        background = Color.White,
                        iconSize = 46,
                        onClick = { onChannelClick(channel) }
                    )
                }
                ShareChannel.VK -> {
                    ShareButton(
                        icon = WHATIcons.VK,
                        color = Color(0xFF2196F3),
                        background = Color.White,
                        iconSize = 34,
                        onClick = { onChannelClick(channel) }
                    )
                }
                ShareChannel.WhatsApp -> {
                    ShareButton(
                        icon = WHATIcons.Whatsapp,
                        color = Color(0xFF4CAF50),
                        background = Color.White,
                        iconSize = 34,
                        onClick = { onChannelClick(channel) }
                    )
                }
                ShareChannel.Clipboard -> {
                    ShareButton(
                        icon = WHATIcons.Copy,
                        color = colorScheme.secondaryContainer,
                        background = colorScheme.onSecondaryContainer,
                        iconSize = 30,
                        onClick = { onChannelClick(channel) }
                    )
                }
            }
        }
    }
}

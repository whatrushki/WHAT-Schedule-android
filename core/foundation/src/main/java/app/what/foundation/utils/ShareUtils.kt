package app.what.foundation.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import app.what.foundation.ui.Gap
import app.what.foundation.ui.controllers.SheetController
import app.what.foundation.ui.controllers.rememberSheetController

sealed interface ShareVariant {
    fun share(context: Context, text: String)

    object Clipboard : ShareVariant {
        override fun share(context: Context, text: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Shared Text", text)
            clipboard.setPrimaryClip(clip)
            // Тут можно кинуть Toast "Скопировано"
        }
    }

    object SystemDefault : ShareVariant {
        override fun share(context: Context, text: String) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, "Поделиться"))
        }
    }

    abstract class DeepLinkMessenger(private val urlTemplate: String) : ShareVariant {
        override fun share(context: Context, text: String) {
            val encodedText = Uri.encode(text) // Важно!
            val uri = urlTemplate.replace("{text}", encodedText).toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri)

            runCatching {
                context.startActivity(intent)
            }.onFailure {
                // Если приложение не установлено, открываем системное "Поделиться"
                SystemDefault.share(context, text)
            }
        }
    }

    abstract class PackageMessenger(private val packageName: String) : ShareVariant {
        override fun share(context: Context, text: String) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                setPackage(packageName)
            }
            runCatching {
                context.startActivity(intent)
            }.onFailure {
                SystemDefault.share(context, text)
            }
        }
    }

    object Telegram : DeepLinkMessenger("tg://msg?text={text}")
    object WhatsApp : DeepLinkMessenger("https://api.whatsapp.com/send?text={text}")
    object VK : PackageMessenger("com.vkontakte.android")
}

object ShareUtils {
    fun share(context: Context, variant: ShareVariant, text: String) {
        variant.share(context, text)
    }

    fun shareUris(context: Context, uris: ArrayList<Uri>, title: String = "Поделиться файлами") {
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, title))
    }
}

@Composable
fun <T> SharePane(
    items: List<T>,
    initialSelection: List<T> = items,
    title: String = "Поделиться",
    itemLabel: (T) -> String,
    onResult: (List<T>) -> ShareContent
) {
    val context = LocalContext.current
    val selectedItems = remember { mutableStateListOf<T>().apply { addAll(initialSelection) } }

    // Получаем финальный контент на основе выбора
    val finalContent = remember(selectedItems.size) { onResult(selectedItems.toList()) }

    Column(Modifier.padding(16.dp).navigationBarsPadding()) {
        Text(title, style = typography.headlineMedium, fontWeight = FontWeight.Bold)
        Gap(16)

        // 1. Секция выбора (можно заменить на FlowRow с Chip-ами или оставить сегменты)
        Text("Выберите элементы:", style = typography.labelLarge)
        Gap(8)

        // Пример с чипами (универсальнее чем сегменты, если элементов много)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                FilterChip(
                    selected = item in selectedItems,
                    onClick = {
                        if (item in selectedItems) selectedItems.remove(item)
                        else selectedItems.add(item)
                    },
                    label = { Text(itemLabel(item)) }
                )
            }
        }

        Gap(24)

        // 2. Секция кнопок
        Text("Куда отправить:", style = typography.labelLarge)
        Gap(12)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ShareIcon(ShareVariant.Telegram, Icons.Default.Share, Color(0xFF2AABEE), finalContent)
            ShareIcon(ShareVariant.VK, Icons.Default.Share, Color(0xFF2196F3), finalContent)
            ShareIcon(ShareVariant.WhatsApp, Icons.Default.Share, Color(0xFF4CAF50), finalContent)
            ShareIcon(ShareVariant.Clipboard, Icons.Default.Share, Color.Gray, finalContent)
            ShareIcon(ShareVariant.SystemDefault, Icons.Default.Share, Color.DarkGray, finalContent)
        }

        Gap(16)
    }
}

@Composable
private fun ShareIcon(
    variant: ShareVariant,
    icon: ImageVector,
    color: Color,
    content: ShareContent
) {
    val context = LocalContext.current
    IconButton(
        onClick = {
            when (content) {
                is ShareContent.Text -> ShareUtils.share(context, variant, content.value)
                is ShareContent.Files -> ShareUtils.shareUris(context, ArrayList(content.uris))
            }
        },
        modifier = Modifier.size(56.dp).background(color.copy(0.1f), CircleShape)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
    }
}

class ShareController(
    private val sheetController: SheetController
) {
    /**
     * @param items Список объектов (дни расписания, новости, заметки)
     * @param itemLabel Как отобразить элемент в кнопке выбора
     * @param onResult Превращение выбранных объектов в текст/файлы
     */
    fun <T> openShare(
        items: List<T>,
        title: String = "Поделиться",
        itemLabel: (T) -> String,
        onResult: (List<T>) -> ShareContent
    ) {
        sheetController.open(full = false) {
            SharePane(
                items = items,
                title = title,
                itemLabel = itemLabel,
                onResult = onResult
            )
        }
    }
}

@Composable
fun rememberShareController(sheetController: SheetController = rememberSheetController()): ShareController {
    return remember(sheetController) { ShareController(sheetController) }
}

sealed interface ShareContent {
    data class Text(val value: String) : ShareContent
    data class Files(val uris: List<Uri>, val message: String? = null) : ShareContent
}
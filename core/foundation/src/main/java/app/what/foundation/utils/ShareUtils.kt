package app.what.foundation.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import app.what.foundation.ui.controllers.SheetController
import app.what.foundation.ui.controllers.rememberSheetController

sealed interface ShareVariant {
    fun share(context: Context, text: String)

    object Clipboard : ShareVariant {
        override fun share(context: Context, text: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Shared Text", text)
            clipboard.setPrimaryClip(clip)
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
            val encodedText = Uri.encode(text)
            val uri = urlTemplate.replace("{text}", encodedText).toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri)

            runCatching {
                context.startActivity(intent)
            }.onFailure {
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
        val channel = when (variant) {
            is ShareVariant.Telegram -> ShareChannel.Telegram
            is ShareVariant.VK -> ShareChannel.VK
            is ShareVariant.WhatsApp -> ShareChannel.WhatsApp
            is ShareVariant.Clipboard -> ShareChannel.Clipboard
            is ShareVariant.SystemDefault -> ShareChannel.SystemDefault
            else -> ShareChannel.SystemDefault
        }
        executeShare(context, channel, ShareData.Text(text))
    }

    fun shareUris(context: Context, uris: ArrayList<Uri>, title: String = "Поделиться файлами") {
        executeShare(context, ShareChannel.SystemDefault, ShareData.Files(uris, title = title))
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
    CustomSharePane(
        title = title,
        items = items,
        initialSelection = initialSelection,
        channels = ShareChannel.defaultChannels,
        itemLabel = itemLabel,
        onShare = { channel, selected ->
            val content = onResult(selected)
            val data = when (content) {
                is ShareContent.Text -> ShareData.Text(content.value)
                is ShareContent.Files -> ShareData.Files(content.uris, text = content.message)
            }
            executeShare(context, channel, data)
        }
    )
}

class ShareController(
    private val sheetController: SheetController
) {
    fun <T> openShare(
        items: List<T>,
        title: String = "Поделиться",
        itemLabel: (T) -> String,
        onResult: (List<T>) -> ShareContent
    ) {
        sheetController.open(full = false) {
            val context = LocalContext.current
            CustomSharePane(
                title = title,
                items = items,
                initialSelection = items,
                channels = ShareChannel.defaultChannels,
                itemLabel = itemLabel,
                onShare = { channel, selected ->
                    val content = onResult(selected)
                    val data = when (content) {
                        is ShareContent.Text -> ShareData.Text(content.value)
                        is ShareContent.Files -> ShareData.Files(content.uris, text = content.message)
                    }
                    executeShare(context, channel, data)
                    sheetController.animateClose()
                }
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
package app.what.schedule.features.newsDetail.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.bclick
import app.what.foundation.utils.ShareUtils
import app.what.foundation.utils.ShareVariant
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Export
import app.what.schedule.ui.theme.icons.filled.Telegram
import app.what.schedule.ui.theme.icons.filled.VK
import app.what.schedule.ui.theme.icons.filled.Whatsapp

val NewsSharePane = @Composable { link: String ->
    val context = LocalContext.current

    Column(
        Modifier.verticalScroll(rememberScrollState())
    ) {
        Text(
            "Поделиться новостью",
            style = typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 42.sp,
            color = colorScheme.primary,
            modifier = Modifier
                .padding(12.dp)
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Gap(12)

            ShareButton(
                icon = WHATIcons.Export,
                color = Color.Gray
            ) {
                ShareUtils.share(
                    context, ShareVariant.Clipboard, link
                )
            }

            Gap(8)

            ShareButton(
                icon = WHATIcons.Telegram,
                iconSize = 44,
                color = Color(0xFF2AABEE)
            ) {
                ShareUtils.share(
                    context, ShareVariant.Telegram, link
                )
            }

            Gap(8)

            ShareButton(
                icon = WHATIcons.VK,
                color = Color(0xFF2196F3)
            ) {
                ShareUtils.share(
                    context, ShareVariant.VK, link
                )
            }

            Gap(8)

            ShareButton(
                icon = WHATIcons.Whatsapp,
                color = Color(0xFF4CAF50)
            ) {
                ShareUtils.share(
                    context, ShareVariant.WhatsApp, link
                )
            }

            Gap(12)
        }

        Gap(16)
    }
}

@Composable
fun ShareButton(
    icon: ImageVector,
    color: Color,
    iconSize: Int = 34,
    onClick: () -> Unit
) = Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
        .size(68.dp)
        .clip(shapes.medium)
        .background(color)
        .bclick(block = onClick)
) {
    icon.Show(
        Modifier.size(iconSize.dp), Color.White
    )
}
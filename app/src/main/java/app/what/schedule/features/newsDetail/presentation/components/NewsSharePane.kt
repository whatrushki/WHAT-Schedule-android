package app.what.schedule.features.newsDetail.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.ui.Gap
import app.what.foundation.utils.ShareUtils
import app.what.foundation.utils.ShareVariant
import app.what.schedule.features.schedule.presentation.components.ShareButton
import app.what.schedule.ui.theme.icons.WHATIcons
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
            modifier = Modifier.padding(12.dp)
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Gap(12)

            listOf(
                Triple(
                    Icons.Default.MoreVert,
                    colorScheme.secondaryContainer,
                    ShareVariant.SystemDefault
                ),
                Triple(WHATIcons.Telegram, Color(0xFF2AABEE), ShareVariant.Telegram),
                Triple(WHATIcons.VK, Color(0xFF2196F3), ShareVariant.VK),
                Triple(WHATIcons.Whatsapp, Color(0xFF4CAF50), ShareVariant.WhatsApp),
            ).forEach {
                ShareButton(
                    icon = it.first,
                    color = it.second,
                    if (it.third == ShareVariant.Telegram) 46 else 34
                ) {
                    ShareUtils.share(context, it.third, link)
                }

                Gap(8)
            }

            Gap(4)
        }

        Gap(16)
    }
}

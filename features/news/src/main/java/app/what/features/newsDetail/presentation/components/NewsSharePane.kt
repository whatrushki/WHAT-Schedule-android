package app.what.schedule.features.newsDetail.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.ui.Gap
import app.what.foundation.utils.ShareChannel
import app.what.foundation.utils.ShareChannelsRow
import app.what.foundation.utils.ShareData
import app.what.foundation.utils.executeShare

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

        Gap(8)

        ShareChannelsRow(
            channels = ShareChannel.defaultChannels,
            onChannelClick = { channel ->
                executeShare(context, channel, ShareData.Text(link, "Новость"))
            }
        )

        Gap(16)
    }
}

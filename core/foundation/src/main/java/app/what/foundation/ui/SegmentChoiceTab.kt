package app.what.foundation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RowScope.SegmentTab(
    index: Int,
    count: Int,
    selected: Boolean,
    icon: ImageVector?,
    label: String?,
    onClick: () -> Unit
) = Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
        .weight(1f)
        .clip(SegmentChoiceTab.itemShape(index = index, count = count))
        .bclick(block = onClick)
        .background(
            if (selected) colorScheme.primary
            else colorScheme.secondaryContainer
        )
) {
    val contentColor = if (selected) colorScheme.onPrimary
    else colorScheme.onSecondaryContainer

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(10.dp, 8.dp)
    ) {
        icon?.Show(
            Modifier.size(16.dp),
            contentColor
        )

        if (label != null) Text(
            label,
            color = contentColor,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 20.sp
        )
    }
}

object SegmentChoiceTab {
    fun itemShape(index: Int, count: Int) = when (index) {
        0 -> RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 8.dp,
            bottomStart = 24.dp,
            bottomEnd = 8.dp
        )

        count - 1 -> RoundedCornerShape(
            topStart = 8.dp,
            topEnd = 24.dp,
            bottomStart = 8.dp,
            bottomEnd = 24.dp
        )

        else -> RoundedCornerShape(8.dp)
    }
}
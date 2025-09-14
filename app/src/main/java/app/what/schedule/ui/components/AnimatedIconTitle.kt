package app.what.schedule.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.animations.wiggle

@Composable
fun AnimatedIconTitle(
    icon: ImageVector,
    name: String,
    modifier: Modifier = Modifier
) = Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
) {
    icon.Show(
        color = colorScheme.primary,
        modifier = Modifier
            .size(18.dp)
            .wiggle(20f)
    )

    Gap(8)

    Text(
        name,
        color = colorScheme.primary,
        fontWeight = FontWeight.Medium
    )
}
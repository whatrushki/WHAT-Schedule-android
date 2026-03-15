package app.what.schedule.features.insts.dgtu.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.bclick


@Composable
fun InfoBlock(
    accentColor: Color,
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = Box(
    modifier
        .clip(shapes.large)
        .background(colorScheme.surfaceContainer)
        .bclick(block = onClick)
) {
    Column(Modifier.padding(12.dp)) {
        Box(
            Modifier
                .clip(CircleShape)
                .background(accentColor.copy(alpha = .2f))
        ) {
            icon.Show(color = accentColor, 34, Modifier.padding(8.dp))
        }

        Gap(8)

        Text(
            title,
            color = colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )

        Text(
            description,
            color = colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}
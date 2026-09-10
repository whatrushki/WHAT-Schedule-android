package app.what.foundation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.what.foundation.ui.Show
import app.what.foundation.ui.bclick

@Composable
fun ShareButton(
    icon: ImageVector,
    color: Color,
    background: Color = Color.White,
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
    icon.Show(background, iconSize)
}

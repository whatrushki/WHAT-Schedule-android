package app.what.foundation.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ImageVector.Show(color: Color = Color.Unspecified, size: Int = 24, modifier: Modifier = Modifier) =
    Icon(modifier = modifier.then(Modifier.size(size.dp)), imageVector = this, tint = color, contentDescription = null)
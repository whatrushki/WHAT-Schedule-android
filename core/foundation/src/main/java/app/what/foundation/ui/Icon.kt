package app.what.foundation.ui

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ImageVector.Show(modifier: Modifier = Modifier, color: Color = Color.Unspecified) =
    Icon(modifier = modifier, imageVector = this, tint = color, contentDescription = null)
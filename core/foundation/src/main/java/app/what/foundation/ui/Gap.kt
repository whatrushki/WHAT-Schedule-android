package app.what.foundation.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.Gap(size: Int, modifier: Modifier = Modifier) =
    VerticalGap(size = size, modifier = modifier)

@Composable
fun RowScope.Gap(size: Int, modifier: Modifier = Modifier) =
    HorizontalGap(size = size, modifier = modifier)

@Composable
fun HorizontalGap(size: Int, modifier: Modifier = Modifier) = Spacer(
    modifier = Modifier
        .width(size.dp)
        .then(modifier)
)

@Composable
fun VerticalGap(size: Int, modifier: Modifier = Modifier) = Spacer(
    modifier = Modifier
        .height(size.dp)
        .then(modifier)
)

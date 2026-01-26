package app.what.foundation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.Gap(size: Dp, modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.height(size))
}

@Composable
fun RowScope.Gap(size: Dp, modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.width(size))
}

@Composable
fun ColumnScope.Gap(size: Int, modifier: Modifier = Modifier) =
    VerticalGap(size = size, modifier = modifier)

@Composable
fun RowScope.Gap(size: Int, modifier: Modifier = Modifier) =
    HorizontalGap(size = size, modifier = modifier)

@Composable
fun HorizontalGap(size: Int, modifier: Modifier = Modifier) = Spacer(
    modifier = Modifier.width(size.dp).then(modifier)
)

@Composable
fun VerticalGap(size: Int, modifier: Modifier = Modifier) = Spacer(
    modifier = Modifier.height(size.dp).then(modifier)
)

@Composable
fun SystemBarsGap(insetsSides: WindowInsetsSides = WindowInsetsSides.Bottom) = Box(
    Modifier.padding(WindowInsets.systemBars.only(insetsSides).asPaddingValues())
)
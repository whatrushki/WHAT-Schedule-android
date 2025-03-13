package app.what.foundation.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SingleChoiceSegmentedButtonRowScope.SegmentTab(
    index: Int,
    count: Int,
    selected: Boolean,
    icon: ImageVector?,
    label: String,
    onClick: () -> Unit
) = SegmentedButton(
    shape = SegmentedButtonDefaults.itemShape(index = index, count = count),
    onClick = onClick,
    selected = selected,
    icon = {
        icon?.let {
            if (selected) Icon(it, contentDescription = label, modifier = Modifier.size(16.dp))
        }
    },
) { Text(label) }

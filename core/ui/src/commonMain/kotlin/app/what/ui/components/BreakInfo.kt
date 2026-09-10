package app.what.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.ui.icons.WHATIcons

@Composable
fun BreakInfo(
    minutes: Int,
    active: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (minutes <= 0) return

    Row(
        modifier = modifier.padding(horizontal = 32.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = WHATIcons.Run,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (active) colorScheme.primary else colorScheme.outline
        )

        Spacer(modifier = Modifier.width(10.dp))

        val hours = minutes / 60
        val mins = minutes % 60

        Text(
            text = buildString {
                append("Перерыв")
                if (hours != 0) append(" $hours ч.")
                if (mins != 0) append(" $mins мин.")
            },
            color = if (active) colorScheme.primary else colorScheme.outline,
            fontSize = 14.sp
        )
    }
}

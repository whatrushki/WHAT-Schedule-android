package app.what.schedule.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.ui.animations.wiggle
import app.what.schedule.R

@Composable
fun Fallback(
    text: String,
    modifier: Modifier = Modifier,
    action: Pair<String, () -> Unit>? = null
) = Column(
    modifier = modifier.padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    val (topPadding, bottomPadding) = when (action == null) {
        true -> 8.dp to 8.dp
        false -> 8.dp to 16.dp
    }

    val fallbackShape = remember {
        RoundedCornerShape(
            topStart = topPadding,
            topEnd = topPadding,
            bottomStart = bottomPadding,
            bottomEnd = bottomPadding
        )
    }

    Image(
        painter = painterResource(R.drawable.il_totoro_friends),
        contentDescription = "Totoro и друзья",
        modifier = Modifier
            .size(200.dp)
            .wiggle(10f),
        contentScale = ContentScale.Crop
    )


    Column(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth(.9f)
            .clip(fallbackShape)
            .background(colorScheme.primaryContainer.copy(alpha = .4f))
            .border(
                1.dp,
                colorScheme.secondary.copy(alpha = .5f),
                fallbackShape
            )
    ) {
        Text(
            text = text,
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )

        action ?: return

        Button(
            onClick = action.second,
            shape = RoundedCornerShape(topStart = topPadding, topEnd = topPadding, 0.dp, 0.dp)
        ) {
            Text(
                text = action.first,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                modifier = modifier.fillMaxSize()
            )
        }
    }
}
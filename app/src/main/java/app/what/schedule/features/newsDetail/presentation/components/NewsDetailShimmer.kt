package app.what.schedule.features.newsDetail.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.what.foundation.ui.Gap
import app.what.foundation.ui.animations.rememberShimmer


@Preview
@Composable

fun NewDetailTitleShimmerPreview() = Column(Modifier.width(500.dp)) {
    val shimmer = rememberShimmer()

    NewDetailTitleShimmer(shimmer)
    Gap(100)
    NewDetailContentShimmer(shimmer)
}

@Composable
fun NewDetailTitleShimmer(shimmer: Brush, modifier: Modifier = Modifier) = Column(
    modifier = modifier,
    horizontalAlignment = Alignment.Start
) {
    Gap(8)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier
                .width(140.dp)
                .height(30.dp)
                .clip(CircleShape)
                .background(shimmer)
        )

        Box(
            Modifier
                .width(120.dp)
                .height(20.dp)
                .clip(CircleShape)
                .background(shimmer)
        )
    }

    Gap(8)

    Box(
        Modifier
            .fillMaxWidth(.85f)
            .height(40.dp)
            .clip(CircleShape)
            .background(shimmer)
    )

    Gap(8)

    repeat(2) {
        Box(
            Modifier
                .fillMaxWidth(if (it < 1) 1f else .6f)
                .height(18.dp)
                .clip(CircleShape)
                .background(shimmer)
        )

        Gap(8)
    }
}

@Composable
fun NewDetailContentShimmer(shimmer: Brush, modifier: Modifier = Modifier) = Column(
    modifier = modifier,
    horizontalAlignment = Alignment.Start
) {
    repeat(2) {
        repeat(3) {
            Box(
                Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth(if (it < 2) 1f else .6f)
                    .height(20.dp)
                    .clip(CircleShape)
                    .background(shimmer)
            )

            Gap(8)
        }

        Gap(8)

        Box(
            Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(shimmer)
        )

        Gap(16)
    }
}
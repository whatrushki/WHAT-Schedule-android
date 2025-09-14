package app.what.schedule.features.schedule.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.what.foundation.ui.Gap
import app.what.foundation.ui.animations.rememberShimmer

@Preview
@Composable
fun ScheduleShimmer() = Column(
    Modifier.padding(horizontal = 12.dp)
) {
    val shimmer = rememberShimmer()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(CircleShape)
            .background(shimmer)
    )

    Gap(8)

    repeat(3) {
        LessonItemShimmer(shimmer)
        Gap(8)
    }
}

@Composable
fun LessonItemShimmer(shimmer: Brush) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(146.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(shimmer)
    ) {
        Row(
            Modifier.padding(8.dp, 12.dp)
        ) {
            Gap(4)

            Column(
                Modifier.width(64.dp)
            ) {
                Gap(4)

                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(shimmer)
                        .size(60.dp, 24.dp)
                )

                Gap(8)

                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(shimmer)
                        .size(40.dp, 20.dp)
                )
            }

            Gap(12)

            Column {
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(shimmer)
                        .height(34.dp)
                        .fillMaxWidth()
                )

                Gap(8)

                repeat(3) {
                    Box(
                        Modifier
                            .clip(CircleShape)
                            .background(shimmer)
                            .size(80.dp, 16.dp)
                    )

                    Gap(8)
                }
            }
        }
    }
}
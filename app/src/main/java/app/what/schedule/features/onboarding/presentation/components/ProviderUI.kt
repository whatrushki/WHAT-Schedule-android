package app.what.schedule.features.onboarding.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.ui.Gap
import app.what.foundation.ui.bclick
import app.what.foundation.ui.capplyIf
import app.what.schedule.data.remote.api.InstitutionProvider


@Composable
fun ProviderUI(
    provider: InstitutionProvider.Factory,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(shapes.medium)
            .bclick { onSelect() }
            .animateContentSize()
            .background(colorScheme.surfaceContainerHigh)
            .capplyIf(selected) { border(2.dp, colorScheme.primary, shapes.medium) }

    ) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                provider.metadata.name,
                style = typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = colorScheme.primary
            )

            Text(
                provider.metadata.description,
                style = typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = colorScheme.primary
            )

            Gap(8)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                provider.metadata.sourceTypes.forEach {
                    AssistChip(
                        onClick = {},
                        label = { Text(it.name) },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = colorScheme.tertiary
                        )
                    )
                }
            }

            AnimatedVisibility(selected) {
                Gap(8)

                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    PropertyList(provider.metadata.advantages, true, Modifier.weight(1f))
                    Gap(2)
                    PropertyList(provider.metadata.disadvantages, false, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PropertyList(
    properties: List<String>,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    val accentColor = if (positive) colorScheme.tertiary else colorScheme.primary

    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        properties.forEach {
            Row {
                Icon(
                    imageVector = if (positive) Icons.Filled.Add else Icons.Filled.Clear,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(14.dp),
                    tint = accentColor
                )

                Gap(4)

                Text(
                    it, color = accentColor,
                    style = typography.bodyMedium,
                    fontSize = 16.sp
                )
            }
        }
    }
}
package app.what.schedule.features.onboarding.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.bclick
import app.what.foundation.ui.capplyIf
import app.what.schedule.data.remote.api.Institution
import app.what.schedule.data.remote.api.InstitutionFilial
import app.what.schedule.data.remote.api.MetaInfo
import app.what.schedule.data.remote.api.SourceType
import app.what.schedule.data.remote.providers.rksi.general.RKSIOfficialProvider
import app.what.schedule.data.remote.providers.rksi.general.RKSITurtleProvider
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Building


@Preview
@Composable
fun InstitutionUIPrev() = Column {
    InstitutionUI(
        data = Institution(
            MetaInfo(
                id = "hse",
                name = "ВШЭ",
                fullName = "Высшая Школа Экономики",
                description = "Высшая Школа Экономики",
                sourceTypes = setOf(SourceType.API, SourceType.EXCEL, SourceType.PDF),
                sourceUrl = "https://hse.ru"
            ),
            listOf(
                InstitutionFilial(
                    MetaInfo(
                        id = "hse_nizhny_novgorod",
                        name = "ВШЭ Нижний Новгород",
                        fullName = "Филиал ВШЭ в Нижнем Новгороде",
                        description = "Филиал ВШЭ в Нижнем Новгороде",
                        sourceTypes = setOf(SourceType.API),
                        sourceUrl = "https://hse.ru/nizhny_novgorod"
                    ),
                    setOf(
                        RKSIOfficialProvider.Factory
                    ),
                    RKSIOfficialProvider.Factory
                ),
                InstitutionFilial(
                    MetaInfo(
                        id = "hse_perm",
                        name = "ВШЭ Пермь",
                        fullName = "Филиал ВШЭ в Перми",
                        description = "Филиал ВШЭ в Перми",
                        sourceTypes = setOf(SourceType.EXCEL),
                        sourceUrl = "https://hse.ru/perm"
                    ),
                    setOf(
                        RKSITurtleProvider.Factory
                    ),
                    RKSITurtleProvider.Factory
                )
            )
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InstitutionUI(
    modifier: Modifier = Modifier,
    data: Institution,
    selected: Boolean = false,
    onInstitutionSelect: (Institution) -> Unit = {},
    onFilialSelect: (InstitutionFilial) -> Unit = {}
) {
    Box(
        modifier
            .clip(shapes.medium)
            .bclick { onInstitutionSelect(data) }
            .animateContentSize()
            .background(colorScheme.surfaceContainerHigh)
            .capplyIf(selected) { border(2.dp, colorScheme.primary, shapes.medium) }
            .fillMaxWidth()
    ) {
        Column(
            Modifier.padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Text(
                data.metadata.name,
                style = typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = colorScheme.primary
            )

            Text(
                data.metadata.fullName,
                style = typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = colorScheme.primary
            )

            AnimatedVisibility(data.filials.size > 1 && selected) {
                Column {
                    Gap(8)

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WHATIcons.Building.Show(Modifier.size(16.dp), colorScheme.secondary)

                        Gap(4)

                        Text(
                            "Субъекты",
                            style = typography.labelLarge,
                            color = colorScheme.secondary
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        data.filials.forEach { filial ->
                            AssistChip(
                                onClick = { onFilialSelect(filial) },
                                label = { Text(filial.metadata.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

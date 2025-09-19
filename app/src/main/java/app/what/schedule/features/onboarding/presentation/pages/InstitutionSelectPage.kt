package app.what.schedule.features.onboarding.presentation.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.what.foundation.ui.Gap
import app.what.foundation.ui.SystemBarsGap
import app.what.foundation.ui.controllers.rememberSheetController
import app.what.foundation.ui.useState
import app.what.schedule.data.remote.api.Institution
import app.what.schedule.data.remote.api.InstitutionFilial
import app.what.schedule.data.remote.api.InstitutionProvider
import app.what.schedule.features.onboarding.presentation.components.InstitutionUI
import app.what.schedule.features.onboarding.presentation.components.ProviderUI
import app.what.schedule.ui.components.SearchBox

@Composable
fun InstitutionSelectPage(
    modifier: Modifier = Modifier,
    institutions: List<Institution>,
    onSelect: (Institution, InstitutionFilial, InstitutionProvider.Factory) -> Unit
) = Column(modifier) {
    var selectedInstitution by useState<Institution?>(null)
    val sheetController = rememberSheetController()
    val (query, setQuery) = useState("")
    val filteredInstitutions = remember(query, institutions.size) {
        institutions.filter {
            it.metadata.name.contains(query, true) ||
                    it.metadata.fullName.contains(query, true)
        }
    }

    SearchBox(query, setQuery)

    Gap(12)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filteredInstitutions, key = { it.metadata.id }) {
            InstitutionUI(
                data = it,
                selected = it == selectedInstitution,
                modifier = Modifier.animateItem(),
                onInstitutionSelect = {
                    selectedInstitution = it
                    if (it.filials.size != 1) return@InstitutionUI

                    sheetController.open(true) @Composable {
                        providerSelectSheet(it, it.filials[0], onSelect)
                    }
                }
            ) { sit ->
                sheetController.open(true) @Composable {
                    providerSelectSheet(it, sit, onSelect)
                }
            }
        }

        item {
            SystemBarsGap()
        }
    }
}


private val providerSelectSheet =
    @Composable { institution: Institution,
                  filial: InstitutionFilial,
                  onSelect: (Institution, InstitutionFilial, InstitutionProvider.Factory) -> Unit ->
        Box {
            val controller = rememberSheetController()
            var provider by useState(filial.defaultProvider)

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    "Выбери провайдера расписания",
                    style = typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                filial.providers.forEach {
                    ProviderUI(it, it == provider) { provider = it }
                }
            }

            Button(
                onClick = { onSelect(institution, filial, provider); controller.animateClose() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
                    .align(Alignment.BottomCenter),
            ) {
                Text("Выбрать")
            }
        }
    }

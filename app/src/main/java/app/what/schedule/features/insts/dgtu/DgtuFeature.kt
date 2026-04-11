package app.what.schedule.features.insts.dgtu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import app.what.foundation.core.Feature
import app.what.navigation.core.NavigationHost
import app.what.navigation.core.rememberHostNavigator
import app.what.navigation.core.rememberNavigator
import app.what.schedule.features.insts.dgtu.domain.DgtuController
import app.what.schedule.features.insts.dgtu.domain.models.DgtuAction
import app.what.schedule.features.insts.dgtu.domain.models.DgtuEvent
import app.what.schedule.features.insts.dgtu.navigation.DGTUAuthProvider
import app.what.schedule.features.insts.dgtu.navigation.DGTUCertificateProvider
import app.what.schedule.features.insts.dgtu.navigation.DGTUEventProvider
import app.what.schedule.features.insts.dgtu.navigation.DGTUMailProvider
import app.what.schedule.features.insts.dgtu.navigation.DGTUMainProvider
import app.what.schedule.features.insts.dgtu.presentation.pages.DGTULoginScreen
import app.what.schedule.features.insts.dgtu.presentation.pages.DGTUMainScreen
import app.what.schedule.features.news.navigation.NewsProvider
import app.what.schedule.features.newsDetail.navigation.NewsDetailProvider
import app.what.schedule.features.schedule.navigation.ScheduleProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DgtuFeature : Feature<DgtuController, DgtuEvent>(), KoinComponent {
    override val controller: DgtuController by inject()
    
    @Composable
    override fun content(modifier: Modifier) {
        val action = controller.collectActions()
        val state = controller.collectStates()
        val nav = rememberHostNavigator()
        val midNav = rememberNavigator(1)
        val globalNav = rememberNavigator(2)
        
        NavigationHost(
            Modifier,
            start = if (state.value.token != null) DGTUMainProvider
            else DGTUAuthProvider,
            navigator = nav,
        ) {
            composable<DGTUAuthProvider> {
                DGTULoginScreen(listener)
            }
            
            composable<DGTUMainProvider> {
                DGTUMainScreen(state, listener)
            }
        }
        
        LaunchedEffect(action.value) {
            val ac = action.value
            ac ?: return@LaunchedEffect
            
            when (ac) {
                is DgtuAction.OpenSchedule -> midNav.c.navigate(
                    ScheduleProvider(
                        ac.search.name,
                        ac.search.id,
                        true
                    )
                )
                
                is DgtuAction.OpenNews -> midNav.c.navigate(NewsProvider)
                is DgtuAction.OpenNewDetail -> globalNav.c.navigate(
                    NewsDetailProvider(ac.id, ac.url, ac.bannerUrl, ac.title, ac.description)
                )
                
                else -> nav.c.navigate(
                    when (action) {
                        DgtuAction.OpenAuth -> DGTUAuthProvider
                        DgtuAction.OpenCertificate -> DGTUCertificateProvider
                        DgtuAction.OpenEvent -> DGTUEventProvider
                        DgtuAction.OpenMail -> DGTUMailProvider
                        DgtuAction.OpenMain -> DGTUMainProvider
                        else -> DGTUMainProvider
                    }
                )
            }
            
            controller.clearAction()
        }
    }
    
}
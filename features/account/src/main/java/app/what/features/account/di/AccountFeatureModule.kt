package app.what.features.account.di

import app.what.foundation.core.Feature
import app.what.schedule.features.insts.dgtu.DgtuFeature
import app.what.schedule.features.insts.dgtu.domain.DgtuController
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val accountFeatureModule = module {
    singleOf(::DgtuController)
    single<Feature<*, *>>(named("dgtuAccount")) { DgtuFeature() }
}

package com.seucaio.unideas.di

import com.seucaio.unideas.data.di.dataModule
import com.seucaio.unideas.domain.di.domainModule
import com.seucaio.unideas.feature.sections.di.sectionsModule
import org.koin.dsl.module

val appModule = module {
    includes(dataModule, domainModule, sectionsModule)
}

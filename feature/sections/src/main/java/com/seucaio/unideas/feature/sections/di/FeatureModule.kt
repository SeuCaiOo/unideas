package com.seucaio.unideas.feature.sections.di

import com.seucaio.unideas.feature.sections.viewmodel.SectionsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sectionsModule = module {
    viewModelOf(::SectionsViewModel)
}

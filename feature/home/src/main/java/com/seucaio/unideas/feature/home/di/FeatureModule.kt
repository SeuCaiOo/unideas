package com.seucaio.unideas.feature.home.di

import com.seucaio.unideas.feature.home.features.allpriorities.viewmodel.AllPrioritiesViewModel
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeViewModel
import com.seucaio.unideas.feature.home.features.priority.viewmodel.PriorityViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    viewModelOf(::PriorityViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::AllPrioritiesViewModel)
}

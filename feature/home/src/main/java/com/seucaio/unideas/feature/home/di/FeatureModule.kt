package com.seucaio.unideas.feature.home.di

import com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    viewModelOf(::HomeViewModel)
}

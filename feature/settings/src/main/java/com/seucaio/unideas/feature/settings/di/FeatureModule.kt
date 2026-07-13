package com.seucaio.unideas.feature.settings.di

import com.seucaio.unideas.feature.settings.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    viewModelOf(::SettingsViewModel)
}

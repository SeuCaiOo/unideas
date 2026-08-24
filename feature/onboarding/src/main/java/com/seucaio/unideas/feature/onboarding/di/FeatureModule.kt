package com.seucaio.unideas.feature.onboarding.di

import com.seucaio.unideas.feature.onboarding.viewmodel.OnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingModule = module {
    viewModelOf(::OnboardingViewModel)
}

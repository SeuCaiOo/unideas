package com.seucaio.unideas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.usecase.onboarding.GetOnboardingSeenUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val getOnboardingSeen: GetOnboardingSeenUseCase,
) : ViewModel() {

    private val _needsOnboarding = MutableStateFlow<Boolean?>(null)
    val needsOnboarding: StateFlow<Boolean?> = _needsOnboarding.asStateFlow()

    init {
        viewModelScope.launch {
            _needsOnboarding.value = !getOnboardingSeen().getOrDefault(false)
        }
    }
}

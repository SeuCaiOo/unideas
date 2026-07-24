package com.seucaio.unideas.core.common.dev

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ScreenVersion {
    V1, V2, V3, V4, V5
}

object DevScreenVersionToggle {
    private val _selectedVersion = MutableStateFlow(ScreenVersion.V1)
    val selectedVersion: StateFlow<ScreenVersion> = _selectedVersion

    fun select(version: ScreenVersion) {
        _selectedVersion.value = version
    }
}

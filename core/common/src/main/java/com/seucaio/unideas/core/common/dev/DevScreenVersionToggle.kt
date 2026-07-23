package com.seucaio.unideas.core.common.dev

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Screen implementation variants available for dev comparison — extend this enum as new POC versions appear. */
enum class ScreenVersion {
    V1, V2, V3, V4, V5
}

/**
 * Dev-only, in-memory (not persisted — resets on process death) selector for comparing a
 * screen's POC implementations (#84) side by side. Only one version is active at a time.
 * Defaults to [ScreenVersion.V1] — the current shipped implementation.
 */
object DevScreenVersionToggle {
    private val _selectedVersion = MutableStateFlow(ScreenVersion.V1)
    val selectedVersion: StateFlow<ScreenVersion> = _selectedVersion

    fun select(version: ScreenVersion) {
        _selectedVersion.value = version
    }
}

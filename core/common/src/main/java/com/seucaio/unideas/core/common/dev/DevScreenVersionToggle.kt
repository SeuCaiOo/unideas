package com.seucaio.unideas.core.common.dev

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Dev-only, in-memory (not persisted — resets on process death) switch for comparing each
 * screen's current implementation against its `V2` clone (#84) while both exist side by side.
 * Temporary: removed once a version is picked and the losing side is deleted.
 */
object DevScreenVersionToggle {
    private val _useV2 = MutableStateFlow(false)
    val useV2: StateFlow<Boolean> = _useV2

    fun set(value: Boolean) {
        _useV2.value = value
    }
}

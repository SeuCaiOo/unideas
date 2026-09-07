package com.seucaio.unideas.feature.home.features.hiddenitems.screen

internal sealed interface HiddenItemsMode {
    data class Gating(val state: HiddenItemsGateState) : HiddenItemsMode
    data object Unlocked : HiddenItemsMode
}

internal sealed interface HiddenItemsGateState {
    data object Authenticating : HiddenItemsGateState
    data class Failed(val messageRes: Int) : HiddenItemsGateState
}

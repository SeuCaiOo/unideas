package com.seucaio.unideas.feature.home.features.allpriorities.viewmodel

/** User interactions on the all-priorities screen. */
sealed interface AllPrioritiesEvent {

    data class OnItemClicked(val itemId: Long) : AllPrioritiesEvent

    data class OnCompleteClicked(val itemId: Long) : AllPrioritiesEvent

    data object OnRetryClicked : AllPrioritiesEvent
}

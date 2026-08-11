package com.seucaio.unideas.feature.home.features.priority.viewmodel

/** User interactions on the priorities Bottom Sheet. */
sealed interface PriorityEvent {

    data class OnItemClicked(val itemId: Long) : PriorityEvent

    data object OnSeeAllClicked : PriorityEvent

    data object OnRetryClicked : PriorityEvent
}

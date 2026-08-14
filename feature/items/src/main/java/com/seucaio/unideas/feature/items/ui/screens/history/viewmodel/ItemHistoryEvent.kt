package com.seucaio.unideas.feature.items.ui.screens.history.viewmodel

sealed interface ItemHistoryEvent {
    data class OnFilterSelected(val filter: HistoryFilter) : ItemHistoryEvent
}

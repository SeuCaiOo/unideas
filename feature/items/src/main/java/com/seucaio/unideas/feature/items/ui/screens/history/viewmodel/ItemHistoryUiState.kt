package com.seucaio.unideas.feature.items.ui.screens.history.viewmodel

import com.seucaio.unideas.domain.model.CompletionStatus
import com.seucaio.unideas.domain.model.ItemCompletionHistory

enum class HistoryFilter {
    ALL,
    ON_TIME,
    LATE,
    WITH_NOTE,
}

data class ItemHistoryUiState(
    val history: List<ItemCompletionHistory> = emptyList(),
    val activeFilter: HistoryFilter = HistoryFilter.ALL,
) {
    val filteredHistory: List<ItemCompletionHistory>
        get() = when (activeFilter) {
            HistoryFilter.ALL -> history
            HistoryFilter.ON_TIME -> history.filter { it.status == CompletionStatus.ON_TIME }
            HistoryFilter.LATE -> history.filter { it.status == CompletionStatus.LATE }
            HistoryFilter.WITH_NOTE -> history.filter { !it.note.isNullOrBlank() }
        }

    val onTimeCount: Int get() = history.count { it.status == CompletionStatus.ON_TIME }
    val lateCount: Int get() = history.count { it.status == CompletionStatus.LATE }
    val missedCount: Int get() = history.count { it.status == CompletionStatus.MISSED }

    val onTimeRatePercent: Int
        get() = if (history.isEmpty()) 0 else onTimeCount * PERCENT / history.size

    /** Consecutive on-time entries counting back from the most recent (history is DESC by date). */
    val currentStreak: Int
        get() = history.takeWhile { it.status == CompletionStatus.ON_TIME }.size

    private companion object {
        const val PERCENT = 100
    }
}

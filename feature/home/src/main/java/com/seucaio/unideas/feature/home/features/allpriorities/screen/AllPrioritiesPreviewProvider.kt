package com.seucaio.unideas.feature.home.features.allpriorities.screen

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.feature.home.R
import com.seucaio.unideas.feature.home.features.allpriorities.viewmodel.AllPrioritiesUiState
import java.time.LocalDate
import java.time.LocalDateTime

class AllPrioritiesPreviewProvider : PreviewParameterProvider<AllPrioritiesUiState> {

    private val createdAt = LocalDateTime.of(2026, 6, 20, 10, 0)

    private fun task(
        id: Long,
        title: String,
        dueDate: LocalDate?,
        recurrence: Recurrence = Recurrence.None,
    ): Item = Item(
        id = id,
        type = ItemType.TASK,
        title = title,
        dueDate = dueDate,
        recurrence = recurrence,
        createdAt = createdAt,
    )

    override val values: Sequence<AllPrioritiesUiState> = sequenceOf(
        AllPrioritiesUiState.Loading,
        AllPrioritiesUiState.Success(
            items = listOf(
                task(1L, "Pagar contas", LocalDate.of(2026, 6, 25)),
                task(2L, "Renovar assinatura", LocalDate.of(2026, 7, 10), Recurrence.Monthly),
                task(3L, "Ligar pro dentista", LocalDate.of(2026, 6, 30)),
            ),
        ),
        AllPrioritiesUiState.Success(items = emptyList()),
        AllPrioritiesUiState.Error(R.string.all_priorities_load_error),
    )
}

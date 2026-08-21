package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import kotlinx.coroutines.flow.Flow

/**
 * Facade for the "Itens arquivados" screen and the bulk archive action in the listing's
 * selection mode — split out of [HomeUseCase] to keep its constructor under the
 * `LongParameterList` threshold (same pattern as `GoogleAuthUseCase`/`BackupUseCase`, #16).
 */
class ItemArchiveUseCase(
    private val setItemArchivedUseCase: SetItemArchivedUseCase,
    private val getArchivedItemsUseCase: GetArchivedItemsUseCase,
) {

    fun getArchivedItems(): Flow<List<Item>> = getArchivedItemsUseCase()

    suspend fun archiveItems(ids: List<Long>, archived: Boolean): Result<Unit> = runCatching {
        ids.forEach { id -> setItemArchivedUseCase(id, archived).getOrThrow() }
    }
}

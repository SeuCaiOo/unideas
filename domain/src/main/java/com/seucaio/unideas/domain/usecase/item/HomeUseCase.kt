package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import com.seucaio.unideas.domain.usecase.section.SetSectionPinnedUseCase
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Convenience facade over the single-purpose use cases the Home priority panel screen (and
 * Browse, which reuses the same ViewModel) needs — one method per operation, each just
 * delegating. No repository access here. Scoped to
 * [com.seucaio.unideas.feature.home.features.panel.viewmodel.HomeViewModel] only; see
 * [ItemDetailUseCase]/[ItemFormUseCase] for the other screens' own subsets — Item's use cases
 * split unevenly across screens, so naming each facade after the screen it serves keeps it
 * obvious where a method is actually used. [com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase]
 * stays a separate constructor parameter (not folded in here) — same as
 * [ItemFormUseCase]/[com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase] on the form
 * screen, since it's cross-entity, not Item-specific.
 */
class HomeUseCase(
    private val getPriorityItemsUseCase: GetPriorityItemsUseCase,
    private val getItemsUseCase: GetItemsUseCase,
    private val completeItemUseCase: CompleteItemUseCase,
    private val hasAnyItemUseCase: HasAnyItemUseCase,
    private val setSectionPinnedUseCase: SetSectionPinnedUseCase,
) {

    fun getPriorityItems(today: LocalDate, dueSoonDays: Int): Flow<List<Item>> =
        getPriorityItemsUseCase(today, dueSoonDays)

    fun getItems(type: ItemType, sectionId: Long?, tagIds: List<Long>): Flow<List<Item>> =
        getItemsUseCase(type, sectionId, tagIds)

    suspend fun complete(item: Item, completedAt: LocalDateTime): Result<CompletionResult> =
        completeItemUseCase(item, completedAt)

    fun hasAnyItem(): Flow<Boolean> = hasAnyItemUseCase()

    suspend fun setSectionPinned(id: Long, isPinned: Boolean): Result<Unit> =
        setSectionPinnedUseCase(id, isPinned)
}

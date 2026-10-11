package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching
import java.time.LocalDateTime

/** Updates an existing [Item], stamping [Item.updatedAt] with [now]; returns the item as persisted. */
class EditItemUseCase(private val repository: ItemRepository) : UseCase {

    suspend operator fun invoke(
        item: Item,
        now: LocalDateTime = LocalDateTime.now()
    ): Result<Item> = resultCatching {
        require(item.title.isNotBlank()) { "Title is required" }
        item.copy(updatedAt = now).also { repository.updateItem(it) }
    }
}

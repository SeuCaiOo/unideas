package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.repository.AutoBackupTrigger
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.resultCatching

/** Creates a new [Item], returning the generated id. */
class CreateItemUseCase(
    private val repository: ItemRepository,
    private val autoBackupTrigger: AutoBackupTrigger,
) : UseCase {

    suspend operator fun invoke(item: Item): Result<Long> = resultCatching {
        require(item.title.isNotBlank()) { "Title is required" }
        val id = repository.insertItem(item)
        autoBackupTrigger.triggerNow()
        id
    }
}

package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow

/** Observes whether the user has any item at all, regardless of type/section/tags. */
class HasAnyItemUseCase(private val repository: ItemRepository) {

    operator fun invoke(): Flow<Boolean> = repository.hasAnyItem()
}

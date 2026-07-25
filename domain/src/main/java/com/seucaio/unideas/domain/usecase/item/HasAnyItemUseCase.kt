package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.logOnError
import kotlinx.coroutines.flow.Flow

/** Observes whether the user has any item at all, regardless of type/section/tags. */
class HasAnyItemUseCase(private val repository: ItemRepository) : UseCase {

    operator fun invoke(): Flow<Boolean> = repository.hasAnyItem().logOnError(this)
}

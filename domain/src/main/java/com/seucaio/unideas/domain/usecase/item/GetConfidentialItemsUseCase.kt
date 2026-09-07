package com.seucaio.unideas.domain.usecase.item

import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.usecase.UseCase
import com.seucaio.unideas.domain.util.logOnError
import kotlinx.coroutines.flow.Flow

/** Observes every confidential item, for the hidden-items screen behind the biometric gate. */
class GetConfidentialItemsUseCase(private val repository: ItemRepository) : UseCase {

    operator fun invoke(): Flow<List<Item>> = repository.getConfidentialItems().logOnError(this)
}

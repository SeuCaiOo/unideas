package com.seucaio.unideas.domain.usecase.settings

import com.seucaio.unideas.domain.repository.DatabaseRepository

/** Debug-only: wipes every row across all tables. */
class ClearDatabaseUseCase(private val repository: DatabaseRepository) {
    suspend operator fun invoke() = repository.clearAll()
}

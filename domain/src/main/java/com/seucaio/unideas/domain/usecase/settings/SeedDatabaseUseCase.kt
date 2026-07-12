package com.seucaio.unideas.domain.usecase.settings

import com.seucaio.unideas.domain.model.SeedScope
import com.seucaio.unideas.domain.repository.DatabaseRepository

/** Debug-only: clears the database and inserts the sample dataset for [SeedScope]. */
class SeedDatabaseUseCase(private val repository: DatabaseRepository) {
    suspend operator fun invoke(scope: SeedScope) = repository.seed(scope)
}

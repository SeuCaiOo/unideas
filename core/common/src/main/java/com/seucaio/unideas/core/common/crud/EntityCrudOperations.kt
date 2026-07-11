package com.seucaio.unideas.core.common.crud

import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import kotlinx.coroutines.flow.Flow

/**
 * The four operations a simple named-entity management screen needs (e.g. Sections, Tags).
 * Each feature adapts its own use cases to this shape once, letting [EntityCrudViewModel] stay
 * entity-agnostic without requiring the domain models themselves to share an interface.
 */
interface EntityCrudOperations<T> {

    fun getAll(): Flow<List<T>>

    suspend fun add(name: String): Result<Long>

    suspend fun rename(item: T, newName: String): Result<Unit>

    suspend fun delete(item: T): Result<DeletionStatus>
}

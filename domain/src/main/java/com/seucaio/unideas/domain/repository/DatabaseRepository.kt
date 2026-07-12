package com.seucaio.unideas.domain.repository

import com.seucaio.unideas.domain.model.SeedScope

/** Debug-only database maintenance — clearing/seeding, implemented in `:data`. */
interface DatabaseRepository {

    /** Deletes every row across all tables. */
    suspend fun clearAll()

    /** Clears the database, then inserts the sample dataset for [scope]. */
    suspend fun seed(scope: SeedScope)
}

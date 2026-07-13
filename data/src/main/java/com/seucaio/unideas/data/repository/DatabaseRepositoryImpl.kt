package com.seucaio.unideas.data.repository

import com.seucaio.unideas.data.local.database.DatabaseSeeder
import com.seucaio.unideas.data.local.database.UnideasDatabase
import com.seucaio.unideas.domain.model.SeedScope
import com.seucaio.unideas.domain.repository.DatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseRepositoryImpl(
    private val database: UnideasDatabase,
    private val seeder: DatabaseSeeder,
) : DatabaseRepository {

    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        database.clearAllTables()
    }

    override suspend fun seed(scope: SeedScope) = withContext(Dispatchers.IO) {
        database.clearAllTables()
        seeder.seed(scope)
    }
}

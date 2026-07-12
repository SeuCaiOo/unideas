package com.seucaio.unideas.data.repository

import com.seucaio.unideas.data.local.database.DatabaseSeeder
import com.seucaio.unideas.data.local.database.UnideasDatabase
import com.seucaio.unideas.domain.model.SeedScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DatabaseRepositoryImplTest {

    private val database: UnideasDatabase = mockk(relaxed = true)
    private val seeder: DatabaseSeeder = mockk()
    private val repository = DatabaseRepositoryImpl(database, seeder)

    @Test
    fun `clearAll clears every table`() = runTest {
        repository.clearAll()

        verify(exactly = 1) { database.clearAllTables() }
    }

    @Test
    fun `seed clears the database before inserting the scope's sample data`() = runTest {
        coEvery { seeder.seed(SeedScope.FULL) } returns Unit

        repository.seed(SeedScope.FULL)

        coVerifyOrder {
            database.clearAllTables()
            seeder.seed(SeedScope.FULL)
        }
    }

    @Test
    fun `seed delegates the scope to the seeder`() = runTest {
        coEvery { seeder.seed(SeedScope.BASIC) } returns Unit

        repository.seed(SeedScope.BASIC)

        coVerify(exactly = 1) { seeder.seed(SeedScope.BASIC) }
    }
}

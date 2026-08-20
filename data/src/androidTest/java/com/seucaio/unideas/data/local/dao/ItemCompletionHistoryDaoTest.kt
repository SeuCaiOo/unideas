package com.seucaio.unideas.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.seucaio.unideas.data.local.database.UnideasDatabase
import com.seucaio.unideas.data.local.entity.ItemCompletionHistoryEntity
import com.seucaio.unideas.data.local.entity.ItemEntity
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemCompletionHistoryDaoTest {

    private lateinit var database: UnideasDatabase
    private lateinit var dao: ItemCompletionHistoryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, UnideasDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.itemCompletionHistoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetHistoryRoundTrips() = runTest {
        val itemId = seedItem()

        dao.insert(ItemCompletionHistoryEntity(itemId = itemId, scheduledDate = 1_000L, completedAt = 1_500L))

        val history = dao.getHistory(itemId).first()

        assertEquals(1, history.size)
        assertEquals(1_000L, history.single().scheduledDate)
        assertEquals(1_500L, history.single().completedAt)
    }

    @Test
    fun getHistoryOrdersByScheduledDateDescending() = runTest {
        val itemId = seedItem()
        dao.insert(ItemCompletionHistoryEntity(itemId = itemId, scheduledDate = 1_000L, completedAt = null))
        dao.insert(ItemCompletionHistoryEntity(itemId = itemId, scheduledDate = 3_000L, completedAt = null))
        dao.insert(ItemCompletionHistoryEntity(itemId = itemId, scheduledDate = 2_000L, completedAt = null))

        val history = dao.getHistory(itemId).first()

        assertEquals(listOf(3_000L, 2_000L, 1_000L), history.map { it.scheduledDate })
    }

    @Test
    fun getHistoryEmitsEmptyListWhenNoneRecorded() = runTest {
        val itemId = seedItem()

        assertTrue(dao.getHistory(itemId).first().isEmpty())
    }

    @Test
    fun missedOccurrenceIsStoredWithNullCompletedAt() = runTest {
        val itemId = seedItem()

        dao.insert(ItemCompletionHistoryEntity(itemId = itemId, scheduledDate = 1_000L, completedAt = null))

        assertNull(dao.getHistory(itemId).first().single().completedAt)
    }

    @Test
    fun updatePersistsChangesToAnExistingEntry() = runTest {
        val itemId = seedItem()
        val id = dao.insert(ItemCompletionHistoryEntity(itemId = itemId, scheduledDate = 1_000L, completedAt = null))

        dao.update(
            ItemCompletionHistoryEntity(id = id, itemId = itemId, scheduledDate = 1_000L, completedAt = 1_500L, note = "Atrasei"),
        )

        val updated = dao.getHistory(itemId).first().single()
        assertEquals(1_500L, updated.completedAt)
        assertEquals("Atrasei", updated.note)
    }

    @Test
    fun deleteByIdRemovesOnlyThatEntry() = runTest {
        val itemId = seedItem()
        val keptId = dao.insert(ItemCompletionHistoryEntity(itemId = itemId, scheduledDate = 1_000L, completedAt = null))
        val removedId = dao.insert(ItemCompletionHistoryEntity(itemId = itemId, scheduledDate = 2_000L, completedAt = null))

        dao.deleteById(removedId)

        val history = dao.getHistory(itemId).first()
        assertEquals(listOf(keptId), history.map { it.id })
    }

    @Test
    fun deletingTheItemCascadesItsHistory() = runTest {
        val itemId = seedItem()
        dao.insert(ItemCompletionHistoryEntity(itemId = itemId, scheduledDate = 1_000L, completedAt = null))

        database.itemDao().deleteById(itemId)

        assertTrue(dao.getHistory(itemId).first().isEmpty())
    }

    private suspend fun seedItem(): Long = database.itemDao().insert(
        ItemEntity(
            type = ItemType.TASK,
            title = "Tarefa recorrente",
            recurrence = Recurrence.Weekly,
            createdAt = 0L,
        ),
    )
}

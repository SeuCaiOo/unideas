package com.seucaio.unideas.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.seucaio.unideas.data.local.database.UnideasDatabase
import com.seucaio.unideas.data.local.entity.ItemEntity
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemLinkDaoTest {

    private lateinit var database: UnideasDatabase
    private lateinit var dao: ItemLinkDao
    private lateinit var itemDao: ItemDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, UnideasDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.itemLinkDao()
        itemDao = database.itemDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertLinkIsVisibleFromBothSides() = runTest {
        val idA = itemDao.insert(task("First"))
        val idB = itemDao.insert(note("Second"))

        dao.insertLink(idA, idB)

        assertEquals(listOf("Second"), dao.getLinkedItems(idA).first().map { it.item.title })
        assertEquals(listOf("First"), dao.getLinkedItems(idB).first().map { it.item.title })
    }

    @Test
    fun insertLinkNormalizesOrderSoReverseInsertIsANoOp() = runTest {
        val idA = itemDao.insert(task("First"))
        val idB = itemDao.insert(note("Second"))

        dao.insertLink(idA, idB)
        dao.insertLink(idB, idA)

        assertEquals(1, dao.getLinkedItems(idA).first().size)
    }

    @Test
    fun deleteLinkRemovesItFromBothSidesRegardlessOfArgumentOrder() = runTest {
        val idA = itemDao.insert(task("First"))
        val idB = itemDao.insert(note("Second"))
        dao.insertLink(idA, idB)

        dao.deleteLink(idB, idA)

        assertTrue(dao.getLinkedItems(idA).first().isEmpty())
        assertTrue(dao.getLinkedItems(idB).first().isEmpty())
    }

    @Test
    fun deletingEitherLinkedItemCascadesTheLinkAway() = runTest {
        val idA = itemDao.insert(task("First"))
        val idB = itemDao.insert(note("Second"))
        dao.insertLink(idA, idB)

        itemDao.deleteById(idA)

        assertTrue(dao.getLinkedItems(idB).first().isEmpty())
    }

    private fun task(title: String): ItemEntity = ItemEntity(
        type = ItemType.TASK,
        title = title,
        recurrence = Recurrence.None,
        createdAt = 1_000L,
    )

    private fun note(title: String): ItemEntity = ItemEntity(
        type = ItemType.NOTE,
        title = title,
        createdAt = 1_000L,
    )
}

package com.seucaio.unideas.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.seucaio.unideas.data.local.database.UnideasDatabase
import com.seucaio.unideas.data.local.entity.ItemEntity
import com.seucaio.unideas.data.local.entity.ItemTagCrossRef
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
class ItemDaoTest {

    private lateinit var database: UnideasDatabase
    private lateinit var dao: ItemDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, UnideasDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.itemDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetItemByIdRoundTrips() = runTest {
        val id = dao.insert(task(title = "Pagar contas"))

        val loaded = dao.getItemById(id).first()

        assertEquals("Pagar contas", loaded?.item?.title)
        assertEquals(ItemType.TASK, loaded?.item?.type)
        assertTrue(loaded?.tags.orEmpty().isEmpty())
    }

    @Test
    fun getItemByIdEmitsNullWhenMissing() = runTest {
        assertNull(dao.getItemById(999L).first())
    }

    @Test
    fun getItemsFiltersByType() = runTest {
        dao.insert(task(title = "tarefa"))
        dao.insert(note(title = "nota"))

        val tasks = dao.getItems(ItemType.TASK, null, emptyList(), 0).first()
        val notes = dao.getItems(ItemType.NOTE, null, emptyList(), 0).first()

        assertEquals(listOf("tarefa"), tasks.map { it.item.title })
        assertEquals(listOf("nota"), notes.map { it.item.title })
    }

    @Test
    fun getItemsFiltersBySection() = runTest {
        seedSection(1L, "Trabalho")
        dao.insert(task(title = "com seção", sectionId = 1L))
        dao.insert(task(title = "sem seção"))

        val filtered = dao.getItems(ItemType.TASK, 1L, emptyList(), 0).first()
        val unfiltered = dao.getItems(ItemType.TASK, null, emptyList(), 0).first()

        assertEquals(listOf("com seção"), filtered.map { it.item.title })
        assertEquals(2, unfiltered.size)
    }

    @Test
    fun getItemsFiltersByAnyOfTheGivenTags() = runTest {
        seedTags(1L to "urgente", 2L to "casa")
        val taggedId = dao.insertItemWithTags(task(title = "com tag"), listOf(1L))
        dao.insert(task(title = "sem tag"))

        val filtered = dao.getItems(ItemType.TASK, null, listOf(1L, 2L), 2).first()

        assertEquals(listOf(taggedId), filtered.map { it.item.id })
        assertEquals(listOf("urgente"), filtered.single().tags.map { it.name })
    }

    @Test
    fun getItemsSortsByCreatedAtDescending() = runTest {
        dao.insert(task(title = "antiga", createdAt = 1_000L))
        dao.insert(task(title = "recente", createdAt = 2_000L))

        val items = dao.getItems(ItemType.TASK, null, emptyList(), 0).first()

        assertEquals(listOf("recente", "antiga"), items.map { it.item.title })
    }

    @Test
    fun getPriorityItemsReturnsOnlyPendingItemsDueOnOrBeforeThresholdOrderedByDueDate() = runTest {
        dao.insert(task(title = "vencida", dueDate = 1_000L))
        dao.insert(task(title = "no limite", dueDate = 3_000L))
        dao.insert(task(title = "futura", dueDate = 9_000L))
        dao.insert(task(title = "concluída", dueDate = 1_000L, completedAt = 2_000L))
        dao.insert(task(title = "sem prazo"))

        val priorities = dao.getPriorityItems(dueOnOrBefore = 3_000L).first()

        assertEquals(listOf("vencida", "no limite"), priorities.map { it.item.title })
    }

    @Test
    fun updateItemWithTagsReplacesFieldsAndTagLinks() = runTest {
        seedTags(1L to "urgente", 2L to "casa")
        val id = dao.insertItemWithTags(task(title = "original"), listOf(1L))
        val stored = dao.getItemById(id).first()!!.item

        dao.updateItemWithTags(stored.copy(title = "editada"), listOf(2L))

        val updated = dao.getItemById(id).first()!!
        assertEquals("editada", updated.item.title)
        assertEquals(listOf("casa"), updated.tags.map { it.name })
    }

    @Test
    fun deleteByIdRemovesItemAndCascadesTagLinks() = runTest {
        seedTags(1L to "urgente")
        val id = dao.insertItemWithTags(task(title = "para excluir"), listOf(1L))

        dao.deleteById(id)

        assertNull(dao.getItemById(id).first())
        assertEquals(0, countCrossRefs(id))
    }

    private fun task(
        title: String,
        sectionId: Long? = null,
        dueDate: Long? = null,
        completedAt: Long? = null,
        createdAt: Long = 1_000L,
    ): ItemEntity = ItemEntity(
        type = ItemType.TASK,
        title = title,
        sectionId = sectionId,
        dueDate = dueDate,
        recurrence = Recurrence.None,
        completedAt = completedAt,
        createdAt = createdAt,
    )

    private fun note(title: String): ItemEntity = ItemEntity(
        type = ItemType.NOTE,
        title = title,
        createdAt = 1_000L,
    )

    private fun seedTags(vararg tags: Pair<Long, String>) {
        tags.forEach { (id, name) ->
            database.openHelper.writableDatabase.execSQL(
                "INSERT INTO tags (id, name) VALUES (?, ?)",
                arrayOf(id, name),
            )
        }
    }

    /** Seeds the `sections` table — `ItemEntity.sectionId` FKs to it. */
    private fun seedSection(id: Long, name: String) {
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO sections (id, name) VALUES (?, ?)",
            arrayOf(id, name),
        )
    }

    private fun countCrossRefs(itemId: Long): Int =
        database.openHelper.readableDatabase
            .query("SELECT COUNT(*) FROM item_tag WHERE itemId = $itemId")
            .use { cursor ->
                cursor.moveToFirst()
                cursor.getInt(0)
            }
}

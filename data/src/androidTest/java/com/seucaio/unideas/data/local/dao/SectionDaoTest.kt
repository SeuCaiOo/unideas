package com.seucaio.unideas.data.local.dao

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.seucaio.unideas.data.local.database.UnideasDatabase
import com.seucaio.unideas.data.local.entity.ItemEntity
import com.seucaio.unideas.data.local.entity.ItemTagCrossRef
import com.seucaio.unideas.data.local.entity.SectionEntity
import com.seucaio.unideas.data.local.entity.TagEntity
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SectionDaoTest {

    private lateinit var database: UnideasDatabase
    private lateinit var dao: SectionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, UnideasDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.sectionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetSectionsRoundTripsOrderedByName() = runTest {
        dao.insert(SectionEntity(name = "Trabalho"))
        dao.insert(SectionEntity(name = "Casa"))

        val sections = dao.getSections().first()

        assertEquals(listOf("Casa", "Trabalho"), sections.map { it.name })
    }

    @Test
    fun updateRenamesSection() = runTest {
        val id = dao.insert(SectionEntity(name = "original"))
        val stored = dao.getSections().first().single()

        dao.update(stored.copy(name = "renomeada"))

        assertEquals(listOf("renomeada"), dao.getSections().first().map { it.name })
        assertEquals(id, dao.getSections().first().single().id)
    }

    @Test
    fun deleteByIdRemovesSection() = runTest {
        val id = dao.insert(SectionEntity(name = "para excluir"))

        dao.deleteById(id)

        assertEquals(emptyList<SectionEntity>(), dao.getSections().first())
    }

    @Test
    fun countLinkedItemsReflectsItemsPointingToTheSection() = runTest {
        val sectionId = dao.insert(SectionEntity(name = "Trabalho"))
        val itemDao = database.itemDao()
        itemDao.insert(task(sectionId = sectionId))
        itemDao.insert(task(sectionId = sectionId))
        itemDao.insert(task(sectionId = null))

        assertEquals(2, dao.countLinkedItems(sectionId))
    }

    @Test
    fun countLinkedItemsIsZeroWithoutLinkedItems() = runTest {
        val sectionId = dao.insert(SectionEntity(name = "vazia"))

        assertEquals(0, dao.countLinkedItems(sectionId))
    }

    @Test
    fun deletingSectionSetsLinkedItemsSectionIdToNull() = runTest {
        val sectionId = dao.insert(SectionEntity(name = "Trabalho"))
        val itemDao = database.itemDao()
        val itemId = itemDao.insert(task(sectionId = sectionId))

        dao.deleteById(sectionId)

        assertNull(itemDao.getItemById(itemId).first()?.item?.sectionId)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertDuplicateNameViolatesUniqueConstraint() = runTest {
        dao.insert(SectionEntity(name = "Trabalho"))

        dao.insert(SectionEntity(name = "Trabalho"))
    }

    @Test
    fun getSectionsByTagsReturnsDistinctSectionsWithAnyMatchingTaggedItem() = runTest {
        val itemDao = database.itemDao()
        val trabalhoId = dao.insert(SectionEntity(name = "Trabalho"))
        val casaId = dao.insert(SectionEntity(name = "Casa"))
        dao.insert(SectionEntity(name = "sem itens marcados"))
        val urgenteId = database.tagDao().insert(TagEntity(name = "urgente"))
        val domesticoId = database.tagDao().insert(TagEntity(name = "doméstico"))

        val trabalhoItem1 = itemDao.insert(task(sectionId = trabalhoId))
        val trabalhoItem2 = itemDao.insert(task(sectionId = trabalhoId))
        val casaItem = itemDao.insert(task(sectionId = casaId))
        itemDao.insertTagCrossRefs(
            listOf(
                ItemTagCrossRef(itemId = trabalhoItem1, tagId = urgenteId),
                ItemTagCrossRef(itemId = trabalhoItem2, tagId = urgenteId), // 2nd link to same tag/section — DISTINCT must collapse it
                ItemTagCrossRef(itemId = casaItem, tagId = domesticoId),
            ),
        )

        assertEquals(listOf("Trabalho"), dao.getSectionsByTags(listOf(urgenteId)).first().map { it.name })
        assertEquals(
            listOf("Casa", "Trabalho"),
            dao.getSectionsByTags(listOf(urgenteId, domesticoId)).first().map { it.name },
        )
    }

    @Test
    fun getSectionsByTagsIsEmptyForEmptyTagList() = runTest {
        val sectionId = dao.insert(SectionEntity(name = "Trabalho"))
        val tagId = database.tagDao().insert(TagEntity(name = "urgente"))
        val itemId = database.itemDao().insert(task(sectionId = sectionId))
        database.itemDao().insertTagCrossRefs(listOf(ItemTagCrossRef(itemId = itemId, tagId = tagId)))

        assertEquals(emptyList<SectionEntity>(), dao.getSectionsByTags(emptyList()).first())
    }

    private fun task(sectionId: Long?): ItemEntity = ItemEntity(
        type = ItemType.TASK,
        title = "tarefa",
        sectionId = sectionId,
        recurrence = Recurrence.None,
        createdAt = 1_000L,
    )
}

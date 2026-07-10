package com.seucaio.unideas.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.seucaio.unideas.data.local.database.UnideasDatabase
import com.seucaio.unideas.data.local.entity.ItemEntity
import com.seucaio.unideas.data.local.entity.ItemTagCrossRef
import com.seucaio.unideas.data.local.entity.TagEntity
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TagDaoTest {

    private lateinit var database: UnideasDatabase
    private lateinit var dao: TagDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, UnideasDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.tagDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetTagsRoundTripsOrderedByName() = runTest {
        dao.insert(TagEntity(name = "urgente"))
        dao.insert(TagEntity(name = "casa"))

        val tags = dao.getTags().first()

        assertEquals(listOf("casa", "urgente"), tags.map { it.name })
    }

    @Test
    fun deleteByIdRemovesTagAndCascadesCrossRefs() = runTest {
        val tagId = dao.insert(TagEntity(name = "urgente"))
        val itemDao = database.itemDao()
        val itemId = itemDao.insert(task())
        itemDao.insertTagCrossRefs(listOf(ItemTagCrossRef(itemId = itemId, tagId = tagId)))

        dao.deleteById(tagId)

        assertEquals(emptyList<TagEntity>(), dao.getTags().first())
        assertEquals(0, dao.countLinkedItems(tagId))
    }

    @Test
    fun countLinkedItemsReflectsCrossRefs() = runTest {
        val tagId = dao.insert(TagEntity(name = "urgente"))
        val itemDao = database.itemDao()
        val itemId1 = itemDao.insert(task())
        val itemId2 = itemDao.insert(task())
        itemDao.insertTagCrossRefs(
            listOf(
                ItemTagCrossRef(itemId = itemId1, tagId = tagId),
                ItemTagCrossRef(itemId = itemId2, tagId = tagId),
            ),
        )

        assertEquals(2, dao.countLinkedItems(tagId))
    }

    @Test
    fun countLinkedItemsIsZeroWithoutCrossRefs() = runTest {
        val tagId = dao.insert(TagEntity(name = "sem vínculo"))

        assertEquals(0, dao.countLinkedItems(tagId))
    }

    private fun task(): ItemEntity = ItemEntity(
        type = ItemType.TASK,
        title = "tarefa",
        recurrence = Recurrence.None,
        createdAt = 1_000L,
    )
}

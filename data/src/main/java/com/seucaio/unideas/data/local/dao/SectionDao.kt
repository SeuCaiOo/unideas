package com.seucaio.unideas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.seucaio.unideas.data.local.entity.SectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionDao {

    @Query("SELECT * FROM sections ORDER BY name ASC")
    fun getSections(): Flow<List<SectionEntity>>

    /** Sections with at least one item tagged with any of [tagIds] — empty [tagIds] matches nothing. */
    @Query(
        """
        SELECT DISTINCT s.* FROM sections s
        INNER JOIN items i ON i.sectionId = s.id
        INNER JOIN item_tag it ON it.itemId = i.id
        WHERE it.tagId IN (:tagIds)
        ORDER BY s.name ASC
        """,
    )
    fun getSectionsByTags(tagIds: List<Long>): Flow<List<SectionEntity>>

    @Insert
    suspend fun insert(section: SectionEntity): Long

    @Update
    suspend fun update(section: SectionEntity)

    @Query("DELETE FROM sections WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM items WHERE sectionId = :sectionId")
    suspend fun countLinkedItems(sectionId: Long): Int
}

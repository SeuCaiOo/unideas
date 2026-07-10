package com.seucaio.unideas.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for the `tags` table.
 *
 * Minimal definition required by [ItemTagCrossRef]'s FK and the
 * `ItemWithTags` relation; Tag CRUD (DAO/repository) belongs to its own
 * sub-issue.
 */
@Entity(
    tableName = "tags",
    indices = [Index(value = ["name"], unique = true)],
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
)

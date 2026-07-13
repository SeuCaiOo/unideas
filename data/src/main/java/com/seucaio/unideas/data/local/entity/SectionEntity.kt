package com.seucaio.unideas.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for the `sections` table.
 */
@Entity(
    tableName = "sections",
    indices = [Index(value = ["name"], unique = true)],
)
data class SectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
)

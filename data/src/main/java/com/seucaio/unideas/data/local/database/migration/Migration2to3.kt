package com.seucaio.unideas.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds [com.seucaio.unideas.data.local.entity.SectionEntity.isPinned], defaulting existing rows to unpinned. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE sections ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
    }
}

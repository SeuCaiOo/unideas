package com.seucaio.unideas.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds [com.seucaio.unideas.data.local.entity.ItemEntity.isPinned], defaulting existing rows to unpinned. */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE items ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
    }
}

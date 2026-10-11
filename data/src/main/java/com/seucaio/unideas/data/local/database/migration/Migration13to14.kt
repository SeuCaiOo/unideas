package com.seucaio.unideas.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds [com.seucaio.unideas.data.local.entity.ItemEntity.updatedAt], backfilling existing rows with their
 * `createdAt` (never edited as far as the app can tell).
 */
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE items ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        database.execSQL("UPDATE items SET updatedAt = createdAt")
    }
}

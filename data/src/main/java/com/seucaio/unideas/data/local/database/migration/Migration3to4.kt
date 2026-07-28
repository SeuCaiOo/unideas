package com.seucaio.unideas.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds [com.seucaio.unideas.data.local.entity.ItemEntity.dueTime] (nullable, seconds-of-day) and
 * [com.seucaio.unideas.data.local.entity.ItemEntity.reminderWarning] (defaulting existing rows to
 * `NONE`).
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE items ADD COLUMN dueTime INTEGER")
        database.execSQL("ALTER TABLE items ADD COLUMN reminderWarning TEXT NOT NULL DEFAULT 'NONE'")
    }
}

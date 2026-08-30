package com.seucaio.unideas.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds [com.seucaio.unideas.data.local.entity.ItemEntity.remindersMuted], defaulting existing rows to `false`. */
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE items ADD COLUMN remindersMuted INTEGER NOT NULL DEFAULT 0")
    }
}

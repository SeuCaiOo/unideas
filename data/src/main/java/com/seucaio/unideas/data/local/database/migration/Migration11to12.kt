package com.seucaio.unideas.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds [com.seucaio.unideas.data.local.entity.ItemEntity.isConfidential], defaulting existing rows to `false`. */
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE items ADD COLUMN isConfidential INTEGER NOT NULL DEFAULT 0")
    }
}

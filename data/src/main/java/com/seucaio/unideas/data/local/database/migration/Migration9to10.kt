package com.seucaio.unideas.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds [com.seucaio.unideas.data.local.entity.ItemEntity.status], defaulting existing rows to `ACTIVE`. */
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE items ADD COLUMN status TEXT NOT NULL DEFAULT 'ACTIVE'")
    }
}

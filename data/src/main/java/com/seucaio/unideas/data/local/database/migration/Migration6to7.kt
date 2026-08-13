package com.seucaio.unideas.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds `items.lastCompletedScheduledDate` (#133) — the `scheduledDate` of a recurring item's most
 * recently completed occurrence, so `Item.isCompleted` can tell "current occurrence already
 * completed" apart from "not yet", without `dueDate` itself having to move on every completion.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE items ADD COLUMN lastCompletedScheduledDate INTEGER")
    }
}

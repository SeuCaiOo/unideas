package com.seucaio.unideas.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds pending-extension tracking to [com.seucaio.unideas.data.local.entity.ItemEntity]
 * (`pendingExtensionOriginalDueDate`, `pendingExtensionCount`) and its resolved counterpart to
 * [com.seucaio.unideas.data.local.entity.ItemCompletionHistoryEntity] (`originalScheduledDate`,
 * `extensionCount`) — existing rows default to "never extended".
 */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE items ADD COLUMN pendingExtensionOriginalDueDate INTEGER")
        database.execSQL("ALTER TABLE items ADD COLUMN pendingExtensionCount INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE item_completion_history ADD COLUMN originalScheduledDate INTEGER")
        database.execSQL("ALTER TABLE item_completion_history ADD COLUMN extensionCount INTEGER NOT NULL DEFAULT 0")
    }
}

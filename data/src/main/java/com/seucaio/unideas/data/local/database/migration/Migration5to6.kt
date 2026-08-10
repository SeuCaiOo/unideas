package com.seucaio.unideas.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Replaces the plain `itemId` index on `item_completion_history` with a unique
 * `(itemId, scheduledDate)` index (#133) — makes double-completing the same occurrence a no-op
 * at the DB layer instead of relying on ViewModel-side debouncing alone.
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP INDEX IF EXISTS index_item_completion_history_itemId")
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_item_completion_history_itemId_scheduledDate " +
                "ON item_completion_history(itemId, scheduledDate)",
        )
    }
}

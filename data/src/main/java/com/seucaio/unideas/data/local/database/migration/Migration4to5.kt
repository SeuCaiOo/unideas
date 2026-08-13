package com.seucaio.unideas.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Creates `item_completion_history` (#126) and deletes existing recurring items
 * (`recurrence != 'NONE'`) — old rows can't be attributed to a series, since completing a
 * recurring item used to spawn a brand new row instead of advancing `dueDate` in place. Non-
 * recurring tasks/notes are untouched.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS item_completion_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                itemId INTEGER NOT NULL,
                scheduledDate INTEGER NOT NULL,
                completedAt INTEGER,
                note TEXT,
                FOREIGN KEY(itemId) REFERENCES items(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_item_completion_history_itemId " +
                "ON item_completion_history(itemId)",
        )
        database.execSQL("DELETE FROM items WHERE recurrence != 'NONE'")
    }
}

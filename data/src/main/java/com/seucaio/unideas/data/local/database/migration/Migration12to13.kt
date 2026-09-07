package com.seucaio.unideas.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds the `item_link` table (see [com.seucaio.unideas.data.local.entity.ItemLinkEntity]). */
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS item_link (
                itemIdA INTEGER NOT NULL,
                itemIdB INTEGER NOT NULL,
                PRIMARY KEY (itemIdA, itemIdB),
                FOREIGN KEY (itemIdA) REFERENCES items(id) ON DELETE CASCADE,
                FOREIGN KEY (itemIdB) REFERENCES items(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_item_link_itemIdB ON item_link(itemIdB)")
    }
}

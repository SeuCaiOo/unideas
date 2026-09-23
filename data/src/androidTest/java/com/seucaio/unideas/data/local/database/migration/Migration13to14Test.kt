package com.seucaio.unideas.data.local.database.migration

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Same hand-built-vN-shape approach as [Migration11to12Test] (`exportSchema = false` since day one,
 * nothing for `MigrationTestHelper` to build from).
 */
@RunWith(AndroidJUnit4::class)
class Migration13to14Test {

    private val dbName = "migration-13-14-test.db"
    private lateinit var helper: SupportSQLiteOpenHelper

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(V13) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE items (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            type TEXT NOT NULL,
                            title TEXT NOT NULL,
                            description TEXT,
                            sectionId INTEGER,
                            dueDate INTEGER,
                            dueTime INTEGER,
                            recurrence TEXT NOT NULL,
                            reminderWarning TEXT NOT NULL,
                            completedAt INTEGER,
                            createdAt INTEGER NOT NULL,
                            lastCompletedScheduledDate INTEGER,
                            isPinned INTEGER NOT NULL DEFAULT 0,
                            pendingExtensionOriginalDueDate INTEGER,
                            pendingExtensionCount INTEGER NOT NULL DEFAULT 0,
                            status TEXT NOT NULL DEFAULT 'ACTIVE',
                            remindersMuted INTEGER NOT NULL DEFAULT 0,
                            isConfidential INTEGER NOT NULL DEFAULT 0
                        )
                        """.trimIndent(),
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            })
            .build()
        helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
    }

    @After
    fun tearDown() {
        helper.close()
        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(dbName)
    }

    @Test
    fun migrationBackfillsUpdatedAtWithCreatedAt() {
        val db = helper.writableDatabase
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (1, 'TASK', 'Existing', 'WEEKLY', 'NONE', 1234)",
        )

        MIGRATION_13_14.migrate(db)

        db.query("SELECT updatedAt FROM items WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1234L, cursor.getLong(cursor.getColumnIndexOrThrow("updatedAt")))
        }
    }

    @Test
    fun migrationAcceptsWritesToTheNewColumn() {
        val db = helper.writableDatabase
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (1, 'TASK', 'Existing', 'WEEKLY', 'NONE', 1234)",
        )

        MIGRATION_13_14.migrate(db)

        db.execSQL("UPDATE items SET updatedAt = 5678 WHERE id = 1")
        db.query("SELECT updatedAt FROM items WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(5678L, cursor.getLong(cursor.getColumnIndexOrThrow("updatedAt")))
        }
    }

    private companion object {
        const val V13 = 13
    }
}

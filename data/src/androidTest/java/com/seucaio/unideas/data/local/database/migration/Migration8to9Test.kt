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
 * Same hand-built-v8-shape approach as [Migration7to8Test] (`exportSchema = false` since day one,
 * nothing for `MigrationTestHelper` to build from).
 */
@RunWith(AndroidJUnit4::class)
class Migration8to9Test {

    private val dbName = "migration-8-9-test.db"
    private lateinit var helper: SupportSQLiteOpenHelper

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(V8) {
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
                            isPinned INTEGER NOT NULL DEFAULT 0
                        )
                        """.trimIndent(),
                    )
                    db.execSQL(
                        """
                        CREATE TABLE item_completion_history (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            itemId INTEGER NOT NULL,
                            scheduledDate INTEGER NOT NULL,
                            completedAt INTEGER,
                            note TEXT
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
    fun migrationDefaultsExistingRowsToNeverExtended() {
        val db = helper.writableDatabase
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (1, 'TASK', 'Existing', 'WEEKLY', 'NONE', 0)",
        )
        db.execSQL(
            "INSERT INTO item_completion_history (id, itemId, scheduledDate, completedAt) " +
                "VALUES (1, 1, 0, 100)",
        )

        MIGRATION_8_9.migrate(db)

        db.query("SELECT pendingExtensionOriginalDueDate, pendingExtensionCount FROM items WHERE id = 1")
            .use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("pendingExtensionOriginalDueDate")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("pendingExtensionCount")))
            }
        db.query("SELECT originalScheduledDate, extensionCount FROM item_completion_history WHERE id = 1")
            .use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("originalScheduledDate")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("extensionCount")))
            }
    }

    @Test
    fun migrationAcceptsWritesToTheNewColumns() {
        val db = helper.writableDatabase
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (1, 'TASK', 'Existing', 'WEEKLY', 'NONE', 0)",
        )

        MIGRATION_8_9.migrate(db)

        db.execSQL(
            "UPDATE items SET pendingExtensionOriginalDueDate = 500, pendingExtensionCount = 2 WHERE id = 1",
        )
        db.query("SELECT pendingExtensionOriginalDueDate, pendingExtensionCount FROM items WHERE id = 1")
            .use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(500, cursor.getInt(cursor.getColumnIndexOrThrow("pendingExtensionOriginalDueDate")))
                assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("pendingExtensionCount")))
            }
    }

    private companion object {
        const val V8 = 8
    }
}

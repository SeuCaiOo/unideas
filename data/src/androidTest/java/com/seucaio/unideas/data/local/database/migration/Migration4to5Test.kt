package com.seucaio.unideas.data.local.database.migration

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Same hand-built-v4-shape approach as [Migration3to4Test] (`exportSchema = false` since day one,
 * nothing for `MigrationTestHelper` to build from).
 */
@RunWith(AndroidJUnit4::class)
class Migration4to5Test {

    private val dbName = "migration-4-5-test.db"
    private lateinit var helper: SupportSQLiteOpenHelper

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(V4) {
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
                            createdAt INTEGER NOT NULL
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
    fun migrationCreatesTheHistoryTableAcceptingWrites() {
        val db = helper.writableDatabase
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (1, 'TASK', 'Recorrente', 'WEEKLY', 'NONE', 0)",
        )

        MIGRATION_4_5.migrate(db)

        db.execSQL(
            "INSERT INTO item_completion_history (itemId, scheduledDate, completedAt, note) " +
                "VALUES (1, 1000, 1500, 'nota')",
        )
        db.query("SELECT itemId, scheduledDate, completedAt, note FROM item_completion_history").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("itemId")))
            assertEquals(1000, cursor.getInt(cursor.getColumnIndexOrThrow("scheduledDate")))
            assertEquals(1500, cursor.getInt(cursor.getColumnIndexOrThrow("completedAt")))
            assertEquals("nota", cursor.getString(cursor.getColumnIndexOrThrow("note")))
        }
    }

    @Test
    fun migrationDeletesExistingRecurringItemsButKeepsNonRecurringOnes() {
        val db = helper.writableDatabase
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (1, 'TASK', 'Recorrente', 'WEEKLY', 'NONE', 0)",
        )
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (2, 'TASK', 'Única', 'NONE', 'NONE', 0)",
        )

        MIGRATION_4_5.migrate(db)

        db.query("SELECT id, title FROM items").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("id")))
            assertEquals("Única", cursor.getString(cursor.getColumnIndexOrThrow("title")))
            assertEquals(false, cursor.moveToNext())
        }
    }

    @Test
    fun migrationAllowsANullCompletedAtForAMissedOccurrence() {
        val db = helper.writableDatabase
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (1, 'TASK', 'Recorrente', 'WEEKLY', 'NONE', 0)",
        )

        MIGRATION_4_5.migrate(db)

        db.execSQL(
            "INSERT INTO item_completion_history (itemId, scheduledDate, completedAt) VALUES (1, 1000, NULL)",
        )
        db.query("SELECT completedAt FROM item_completion_history").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertNull(cursor.getString(cursor.getColumnIndexOrThrow("completedAt")))
        }
    }

    private companion object {
        const val V4 = 4
    }
}

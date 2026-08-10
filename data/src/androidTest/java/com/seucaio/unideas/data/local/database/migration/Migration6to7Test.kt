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
 * Same hand-built-v6-shape approach as [Migration5to6Test] (`exportSchema = false` since day one,
 * nothing for `MigrationTestHelper` to build from).
 */
@RunWith(AndroidJUnit4::class)
class Migration6to7Test {

    private val dbName = "migration-6-7-test.db"
    private lateinit var helper: SupportSQLiteOpenHelper

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(V6) {
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
    fun migrationAddsTheColumnAsNullForExistingRows() {
        val db = helper.writableDatabase
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (1, 'TASK', 'Recorrente', 'WEEKLY', 'NONE', 0)",
        )

        MIGRATION_6_7.migrate(db)

        db.query("SELECT lastCompletedScheduledDate FROM items WHERE id = 1").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertNull(cursor.getString(cursor.getColumnIndexOrThrow("lastCompletedScheduledDate")))
        }
    }

    @Test
    fun migrationAcceptsWritesToTheNewColumn() {
        val db = helper.writableDatabase
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (1, 'TASK', 'Recorrente', 'WEEKLY', 'NONE', 0)",
        )

        MIGRATION_6_7.migrate(db)

        db.execSQL("UPDATE items SET lastCompletedScheduledDate = 1000 WHERE id = 1")
        db.query("SELECT lastCompletedScheduledDate FROM items WHERE id = 1").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(1000, cursor.getInt(cursor.getColumnIndexOrThrow("lastCompletedScheduledDate")))
        }
    }

    private companion object {
        const val V6 = 6
    }
}

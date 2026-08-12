package com.seucaio.unideas.data.local.database.migration

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Same hand-built-v7-shape approach as [Migration6to7Test] (`exportSchema = false` since day one,
 * nothing for `MigrationTestHelper` to build from).
 */
@RunWith(AndroidJUnit4::class)
class Migration7to8Test {

    private val dbName = "migration-7-8-test.db"
    private lateinit var helper: SupportSQLiteOpenHelper

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(V7) {
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
                            lastCompletedScheduledDate INTEGER
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
    fun migrationDefaultsExistingRowsToUnpinned() {
        val db = helper.writableDatabase
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (1, 'TASK', 'Existing', 'NONE', 'NONE', 0)",
        )

        MIGRATION_7_8.migrate(db)

        db.query("SELECT isPinned FROM items WHERE id = 1").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("isPinned")))
        }
    }

    @Test
    fun migrationAcceptsWritesToTheNewColumn() {
        val db = helper.writableDatabase
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (1, 'TASK', 'Existing', 'NONE', 'NONE', 0)",
        )

        MIGRATION_7_8.migrate(db)

        db.execSQL("UPDATE items SET isPinned = 1 WHERE id = 1")
        db.query("SELECT isPinned FROM items WHERE id = 1").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("isPinned")))
        }
    }

    private companion object {
        const val V7 = 7
    }
}

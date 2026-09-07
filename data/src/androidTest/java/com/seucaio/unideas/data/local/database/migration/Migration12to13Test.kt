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

/** Same hand-built-vN-shape approach as [Migration11to12Test]. */
@RunWith(AndroidJUnit4::class)
class Migration12to13Test {

    private val dbName = "migration-12-13-test.db"
    private lateinit var helper: SupportSQLiteOpenHelper

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(V12) {
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
    fun migrationCreatesItemLinkTableAcceptingWrites() {
        val db = helper.writableDatabase
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (1, 'TASK', 'First', 'WEEKLY', 'NONE', 0)",
        )
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (2, 'NOTE', 'Second', 'WEEKLY', 'NONE', 0)",
        )

        MIGRATION_12_13.migrate(db)

        db.execSQL("INSERT INTO item_link (itemIdA, itemIdB) VALUES (1, 2)")
        db.query("SELECT itemIdA, itemIdB FROM item_link").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("itemIdA")))
            assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("itemIdB")))
        }
    }

    @Test
    fun migrationCascadesDeleteFromEitherSide() {
        val db = helper.writableDatabase
        db.execSQL("PRAGMA foreign_keys = ON")
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (1, 'TASK', 'First', 'WEEKLY', 'NONE', 0)",
        )
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (2, 'NOTE', 'Second', 'WEEKLY', 'NONE', 0)",
        )

        MIGRATION_12_13.migrate(db)

        db.execSQL("INSERT INTO item_link (itemIdA, itemIdB) VALUES (1, 2)")
        db.execSQL("DELETE FROM items WHERE id = 1")

        db.query("SELECT COUNT(*) FROM item_link").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }

    private companion object {
        const val V12 = 12
    }
}

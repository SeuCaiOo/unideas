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
 * The project never exported Room schema JSON files (`exportSchema = false` since day one), so
 * there's nothing for `MigrationTestHelper` to build a v3 database from. Instead, this test builds
 * a plain SQLite `items` table matching the v3 shape by hand, runs [MIGRATION_3_4] directly against
 * it, and asserts the resulting columns/defaults.
 */
@RunWith(AndroidJUnit4::class)
class Migration3to4Test {

    private val dbName = "migration-3-4-test.db"
    private lateinit var helper: SupportSQLiteOpenHelper

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(V3) {
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
                            recurrence TEXT NOT NULL,
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
    fun `migration adds nullable dueTime and reminderWarning defaulting to NONE`() {
        val db = helper.writableDatabase
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, createdAt) VALUES (1, 'TASK', 'Existing', 'NONE', 0)",
        )

        MIGRATION_3_4.migrate(db)

        db.query("SELECT dueTime, reminderWarning FROM items WHERE id = 1").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertNull(cursor.getString(cursor.getColumnIndexOrThrow("dueTime")))
            assertEquals("NONE", cursor.getString(cursor.getColumnIndexOrThrow("reminderWarning")))
        }
    }

    @Test
    fun `migrated table accepts writes to the new columns`() {
        val db = helper.writableDatabase
        MIGRATION_3_4.migrate(db)

        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, createdAt, dueTime, reminderWarning) " +
                "VALUES (2, 'TASK', 'New', 'NONE', 0, 43200, 'DAYS_BEFORE:2')",
        )

        db.query("SELECT dueTime, reminderWarning FROM items WHERE id = 2").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(43200, cursor.getInt(cursor.getColumnIndexOrThrow("dueTime")))
            assertEquals("DAYS_BEFORE:2", cursor.getString(cursor.getColumnIndexOrThrow("reminderWarning")))
        }
    }

    private companion object {
        const val V3 = 3
    }
}

package com.seucaio.unideas.data.local.database.migration

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Same hand-built-v5-shape approach as [Migration4to5Test] (`exportSchema = false` since day one,
 * nothing for `MigrationTestHelper` to build from).
 */
@RunWith(AndroidJUnit4::class)
class Migration5to6Test {

    private val dbName = "migration-5-6-test.db"
    private lateinit var helper: SupportSQLiteOpenHelper

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(V5) {
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
                    db.execSQL(
                        """
                        CREATE TABLE item_completion_history (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            itemId INTEGER NOT NULL,
                            scheduledDate INTEGER NOT NULL,
                            completedAt INTEGER,
                            note TEXT,
                            FOREIGN KEY(itemId) REFERENCES items(id) ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )
                    db.execSQL(
                        "CREATE INDEX index_item_completion_history_itemId " +
                            "ON item_completion_history(itemId)",
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
    fun migrationRejectsADuplicateItemIdAndScheduledDatePair() {
        val db = helper.writableDatabase
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (1, 'TASK', 'Recorrente', 'WEEKLY', 'NONE', 0)",
        )
        db.execSQL(
            "INSERT INTO item_completion_history (itemId, scheduledDate, completedAt) VALUES (1, 1000, 1500)",
        )

        MIGRATION_5_6.migrate(db)

        assertThrows(SQLiteConstraintException::class.java) {
            db.execSQL(
                "INSERT INTO item_completion_history (itemId, scheduledDate, completedAt) VALUES (1, 1000, 1600)",
            )
        }
    }

    @Test
    fun migrationStillAllowsTheSameItemOnDifferentScheduledDates() {
        val db = helper.writableDatabase
        db.execSQL(
            "INSERT INTO items (id, type, title, recurrence, reminderWarning, createdAt) " +
                "VALUES (1, 'TASK', 'Recorrente', 'WEEKLY', 'NONE', 0)",
        )
        db.execSQL(
            "INSERT INTO item_completion_history (itemId, scheduledDate, completedAt) VALUES (1, 1000, 1500)",
        )

        MIGRATION_5_6.migrate(db)

        db.execSQL(
            "INSERT INTO item_completion_history (itemId, scheduledDate, completedAt) VALUES (1, 2000, 2500)",
        )
        db.query("SELECT COUNT(*) FROM item_completion_history").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(2, cursor.getInt(0))
        }
    }

    @Test
    fun migrationDropsTheOldPlainItemIdIndex() {
        val db = helper.writableDatabase

        MIGRATION_5_6.migrate(db)

        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name = 'item_completion_history'",
        ).use { cursor ->
            val indexNames = generateSequence { if (cursor.moveToNext()) cursor.getString(0) else null }.toList()
            assertEquals(false, indexNames.contains("index_item_completion_history_itemId"))
            assertEquals(true, indexNames.contains("index_item_completion_history_itemId_scheduledDate"))
        }
    }

    private companion object {
        const val V5 = 5
    }
}

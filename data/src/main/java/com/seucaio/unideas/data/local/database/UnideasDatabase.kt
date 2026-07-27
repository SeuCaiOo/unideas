package com.seucaio.unideas.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.seucaio.unideas.data.local.converter.Converters
import com.seucaio.unideas.data.local.dao.ItemDao
import com.seucaio.unideas.data.local.dao.SectionDao
import com.seucaio.unideas.data.local.dao.TagDao
import com.seucaio.unideas.data.local.database.migration.MIGRATION_2_3
import com.seucaio.unideas.data.local.database.migration.MIGRATION_3_4
import com.seucaio.unideas.data.local.entity.ItemEntity
import com.seucaio.unideas.data.local.entity.ItemTagCrossRef
import com.seucaio.unideas.data.local.entity.SectionEntity
import com.seucaio.unideas.data.local.entity.TagEntity

/**
 * App Room database.
 *
 * Manual singleton (`@Volatile` + `synchronized`) via [getInstance] in
 * addition to the Koin registration, guaranteeing a single instance even
 * outside the DI graph (e.g. instrumented tests).
 *
 * Every `version` bump ships a real `Migration` (`data/local/database/migration/`), added via
 * `.addMigrations(...)` — deliberately **no** `fallbackToDestructiveMigration()`. The app is
 * local-only (Drive backup/restore is manual, opt-in, not automatic sync), so a missing migration
 * failing loud (Room throws on open, already Timber/Crashlytics-logged via
 * [com.seucaio.unideas.domain.util.logOnError]) is far safer than one silently wiping every item,
 * section, and tag a user has ever saved. A build that reaches users without the right migration
 * is a bug to fix and ship, never a reason to erase their data.
 */
@Database(
    entities = [
        ItemEntity::class,
        SectionEntity::class,
        TagEntity::class,
        ItemTagCrossRef::class,
    ],
    version = 4,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class UnideasDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    abstract fun sectionDao(): SectionDao

    abstract fun tagDao(): TagDao

    companion object {
        const val DATABASE_NAME = "unideas.db"

        @Volatile
        private var instance: UnideasDatabase? = null

        fun getInstance(context: Context): UnideasDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }

        /** Drops the cached singleton so the next [getInstance] rebuilds it — used after a restore. */
        fun resetInstance() {
            instance = null
        }

        /**
         * Forces the WAL log to flush into the main `.db` file before it's copied for backup.
         * `SQLiteDatabase.query` is lazy — the PRAGMA only actually runs once the cursor is read
         * (`moveToFirst()`), not just opened and closed. Without that, the main file stayed at
         * its empty just-created size (4096 bytes) forever, and every backup silently uploaded
         * an empty database.
         */
        fun checkpoint(database: UnideasDatabase) {
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
        }

        private fun buildDatabase(context: Context): UnideasDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                UnideasDatabase::class.java,
                DATABASE_NAME,
            )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .build()
    }
}

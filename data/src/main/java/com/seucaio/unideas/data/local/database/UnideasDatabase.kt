package com.seucaio.unideas.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.seucaio.unideas.data.local.converter.Converters
import com.seucaio.unideas.data.local.dao.ItemDao
import com.seucaio.unideas.data.local.entity.ItemEntity
import com.seucaio.unideas.data.local.entity.ItemTagCrossRef
import com.seucaio.unideas.data.local.entity.TagEntity

/**
 * App Room database.
 *
 * Manual singleton (`@Volatile` + `synchronized`) via [getInstance] in
 * addition to the Koin registration, guaranteeing a single instance even
 * outside the DI graph (e.g. instrumented tests).
 */
@Database(
    entities = [
        ItemEntity::class,
        TagEntity::class,
        ItemTagCrossRef::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class UnideasDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {
        private const val DATABASE_NAME = "unideas.db"

        @Volatile
        private var instance: UnideasDatabase? = null

        fun getInstance(context: Context): UnideasDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }

        private fun buildDatabase(context: Context): UnideasDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                UnideasDatabase::class.java,
                DATABASE_NAME,
            ).build()
    }
}

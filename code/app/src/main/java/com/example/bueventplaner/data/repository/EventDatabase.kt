package com.example.bueventplaner.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bueventplaner.data.model.EventEntity
import com.example.bueventplaner.utils.Converters
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [EventEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: EventDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE event_table ADD COLUMN eventUrl TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE event_table ADD COLUMN savedUsers TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): EventDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "event_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun clearRoomDatabase(eventDao: EventDao) {
            eventDao.deleteAllEvents()
        }
    }

}

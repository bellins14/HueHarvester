package com.example.hueharvester.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

/**
 * Annotates class to be a [Room] Database with a table (entity) of the [ColorData] class
 * @see RoomDatabase
 * @see ColorData
 * @see ColorDataDao
 */
@Database(entities = [ColorData::class], version = 3, exportSchema = false)
abstract class ColorDataDatabase : RoomDatabase() {
    abstract fun colorDataDao(): ColorDataDao

    companion object {
        @Volatile
        private var INSTANCE: ColorDataDatabase? = null

        fun getDatabase(
            context: Context
        ): ColorDataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ColorDataDatabase::class.java,
                    "color_data_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.hueharvester.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

// TODO: commenta bene e documenta
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

    // Populate Database with sample data - only for DEBUGGING
    /*private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.colorDataDao())
                }
            }
        }

        suspend fun populateDatabase(colorDataDao: ColorDataDao) {
            val currentTime = System.currentTimeMillis()
            colorDataDao.deleteAll()
            val color1 = ColorData(timestamp = currentTime - 5*60*1000, red = 150, green = 100, blue = 50)
            colorDataDao.insert(color1)
            val color2 = ColorData(timestamp = currentTime - 4*60*1000, red = 150, green = 100, blue = 50)
            colorDataDao.insert(color2)
            val color3 = ColorData(timestamp = currentTime - 3*60*1000, red = 150, green = 100, blue = 50)
            colorDataDao.insert(color3)
            val color4 = ColorData(timestamp = currentTime - 2*60*1000, red = 150, green = 100, blue = 50)
            colorDataDao.insert(color4)
            val color5 = ColorData(timestamp = currentTime - 1*60*1000, red = 150, green = 100, blue = 50)
            colorDataDao.insert(color5)
        }
    }*/
}


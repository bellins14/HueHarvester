package com.example.hueharvester

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ColorData::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun colorDataDao(): ColorDataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "color_data_database"
                ).addCallback(AppDatabaseCallback(scope)).build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // If you want to keep the data through app restarts,
            // comment out the following line.
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.colorDataDao())
                }
            }
        }

        suspend fun populateDatabase(colorDataDao: ColorDataDao) {
            // Add sample colors - only for debug // TODO: remove this block
            val currentTime = System.currentTimeMillis().toFloat() / 1000f / 60f
            colorDataDao.deleteAll()
            val color1 = ColorData(timestamp = currentTime-5f, red = 150, green = 100, blue = 50)
            colorDataDao.insert(color1)
            val color2 = ColorData(timestamp = currentTime-4f, red = 150, green = 100, blue = 50)
            colorDataDao.insert(color2)
            val color3 = ColorData(timestamp = currentTime-3f, red = 150, green = 100, blue = 50)
            colorDataDao.insert(color3)
            val color4 = ColorData(timestamp = currentTime-2f, red = 150, green = 100, blue = 50)
            colorDataDao.insert(color4)
            val color5 = ColorData(timestamp = currentTime-1f, red = 150, green = 100, blue = 50)
            colorDataDao.insert(color5)
        }
    }
}


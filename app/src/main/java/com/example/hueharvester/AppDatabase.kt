package com.example.hueharvester

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ColorData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun colorDataDao(): ColorDataDao
}

package com.example.hueharvester

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ColorDataDao {
    @Insert
    suspend fun insert(colorData: ColorData)

    @Query("SELECT * FROM color_data WHERE timestamp > :timeLimit ORDER BY timestamp ASC")
    suspend fun getRecentColorData(timeLimit: Long): List<ColorData>
}

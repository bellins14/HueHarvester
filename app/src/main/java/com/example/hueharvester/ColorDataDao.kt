package com.example.hueharvester

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ColorDataDao {
    @Insert
    suspend fun insert(colorData: ColorData)

    @Query("SELECT * FROM color_data ORDER BY timestamp ASC")
    fun getAllData(): Flow<List<ColorData>>

    @Query("SELECT * FROM color_data WHERE timestamp >= :startTime")
    suspend fun getDataFromLastFiveMinutes(startTime: Float): List<ColorData>

    @Query("SELECT * FROM color_data ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastInsertedData(): ColorData?

    @Query("DELETE FROM color_data WHERE timestamp < :startTime")
    suspend fun deleteOldData(startTime: Float)
    @Query("DELETE FROM color_data")
    suspend fun deleteAll()
}

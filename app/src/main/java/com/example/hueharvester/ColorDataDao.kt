package com.example.hueharvester

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ColorDataDao {
    @Insert
    suspend fun insert(colorData: ColorData)

    @Query("SELECT * FROM color_data WHERE timestamp >= :startId ORDER BY id ASC")
    suspend fun getDataAfter(startId: Int): List<ColorData>

    @Query("SELECT * FROM color_data ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastInsertedData(): ColorData?

    @Query("DELETE FROM color_data WHERE id < :startId")
    suspend fun deleteOldData(startId: Int)

    @Query("DELETE FROM color_data")
    suspend fun deleteAll()
}

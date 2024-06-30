package com.example.hueharvester.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the [ColorData] class.
 * @see Dao
 */
@Dao
interface ColorDataDao {
    @Insert
    suspend fun insert(colorData: ColorData)

    @Query("SELECT * FROM color_data ORDER BY id ASC")
    fun getAllData(): Flow<List<ColorData>>

    @Query("DELETE FROM color_data WHERE id < :startId")
    suspend fun deleteOldData(startId: Int)

    @Query("DELETE FROM color_data")
    suspend fun deleteAll()
}

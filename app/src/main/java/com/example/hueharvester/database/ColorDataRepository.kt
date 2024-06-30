package com.example.hueharvester.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

/**
 * Class that handles the data operations with [Room].
 * This class is a Singleton.
 * @param colorDataDao The DAO for the [ColorData] class.
 * @property allColorData A [Flow] of all [ColorData] objects in the database.
 * @see ColorDataDao
 * @see ColorData
 * @constructor Creates a new [ColorDataRepository] with the given [ColorDataDao].
 */
class ColorDataRepository(private val colorDataDao: ColorDataDao) {

    val allColorData: Flow<List<ColorData>> = colorDataDao.getAllData()

    @WorkerThread
    suspend fun insert(colorData: ColorData) {
        colorDataDao.insert(colorData)
    }

    @WorkerThread
    suspend fun deleteOldData(startId: Int) {
        colorDataDao.deleteOldData(startId)
    }
}

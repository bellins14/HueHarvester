package com.example.hueharvester.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

// TODO: commenta bene e documenta

class ColorDataRepository(private val colorDataDao: ColorDataDao) {
    val allColorData: Flow<List<ColorData>> = colorDataDao.getAllData()

    @WorkerThread
    suspend fun insert(colorData: ColorData) {
        colorDataDao.insert(colorData)
    }

    /*@WorkerThread
    suspend fun getDataAfter(startId: Int): List<ColorData> {
        return colorDataDao.getDataAfter(startId)
    }

    @WorkerThread
    suspend fun getLastInsertedData(): ColorData? {
        return colorDataDao.getLastInsertedData()
    }*/

    @WorkerThread
    suspend fun deleteOldData(startId: Int) {
        colorDataDao.deleteOldData(startId)
    }
}
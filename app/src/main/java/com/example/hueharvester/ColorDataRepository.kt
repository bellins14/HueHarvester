package com.example.hueharvester

import androidx.annotation.WorkerThread

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class ColorDataRepository(private val colorDataDao: ColorDataDao) {
    // By default Room runs suspend queries off the main thread. We don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    suspend fun insert(colorData: ColorData) {
        colorDataDao.insert(colorData)
    }

    @WorkerThread
    suspend fun getDataAfter(startTime: Long): List<ColorData> {
        return colorDataDao.getData(startTime)
    }

    @WorkerThread
    suspend fun getLastInsertedData(): ColorData? {
        return colorDataDao.getLastInsertedData()
    }

    @WorkerThread
    suspend fun deleteOldData(startTime: Long) {
        colorDataDao.deleteOldData(startTime)
    }
}
package com.example.hueharvester

import android.app.Application
import com.example.hueharvester.database.ColorDataDatabase
import com.example.hueharvester.database.ColorDataRepository

class ColorDataApplication : Application() {
    private val database by lazy { ColorDataDatabase.getDatabase(this) }
    val repository by lazy { ColorDataRepository(database.colorDataDao()) }
}

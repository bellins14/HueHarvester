package com.example.hueharvester

import android.app.Application
import com.example.hueharvester.database.ColorDataDatabase
import com.example.hueharvester.database.ColorDataRepository

/** Application class for the ColorData application
 *  @property database The database for the application
 *  @property repository The repository for the application
 *  @see Application
 *  @see ColorDataDatabase
 *  @see ColorDataRepository
 *
 */
class ColorDataApplication : Application() {
    private val database by lazy { ColorDataDatabase.getDatabase(this) }
    val repository by lazy { ColorDataRepository(database.colorDataDao()) }
}

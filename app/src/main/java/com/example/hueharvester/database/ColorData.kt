package com.example.hueharvester.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.annotation.IntRange

/**
 * Data class that represents the data structure of the color_data table in the database.
 * @constructor Creates a new ColorData object.
 * @param id The unique identifier for the data entry.
 * @param timestamp The timestamp of the data entry.
 * @param red The red value of the color.
 * @param green The green value of the color.
 * @param blue The blue value of the color.
 * @see Entity
 */
@Entity(tableName = "color_data")
data class ColorData(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,

    val timestamp: Long,
    @IntRange(from = 0, to = 255)
    val red: Int,
    @IntRange(from = 0, to = 255)
    val green: Int,
    @IntRange(from = 0, to = 255)
    val blue: Int
)

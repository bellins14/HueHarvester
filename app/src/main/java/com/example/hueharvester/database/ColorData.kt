package com.example.hueharvester.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.annotation.IntRange

// TODO: commenta bene e documenta
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

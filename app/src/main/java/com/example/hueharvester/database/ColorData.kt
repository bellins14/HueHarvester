package com.example.hueharvester.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// TODO: commenta bene e documenta
@Entity(tableName = "color_data")
data class ColorData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val red: Int,
    val green: Int,
    val blue: Int
)
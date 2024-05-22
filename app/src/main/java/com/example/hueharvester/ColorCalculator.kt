package com.example.hueharvester

import kotlin.experimental.and

class ColorCalculator {
    fun calculateAverageColor(data: ByteArray, width: Int, height: Int): Triple<Int, Int, Int> {
        var redSum = 0
        var greenSum = 0
        var blueSum = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val r = data[index] and 0xFF.toByte()
                val g = data[index + 1] and 0xFF.toByte()
                val b = data[index + 2] and 0xFF.toByte()

                redSum += r
                greenSum += g
                blueSum += b
            }
        }

        val pixelCount = width * height
        return Triple(redSum / pixelCount, greenSum / pixelCount, blueSum / pixelCount)
    }

}
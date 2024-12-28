package com.example.coloredballs

import android.graphics.Color

object Colors {
    private val colorsMap = mutableMapOf(
        "PastelBrown" to Color.parseColor("#FFF476"),
        "lightPink" to Color.parseColor("#F2C14E"),
        "GreenBeige" to Color.parseColor("#F78154"),
        "DarkGreen" to Color.parseColor("#4D9078"),
        "CalmGreen" to Color.parseColor("#B4436C"),
        "IntenseRed" to Color.parseColor("#FF689B"),
        "Russet" to Color.parseColor("#146AD7")
    )

    private var initialColor: Int? = null

    fun getRandomColor(availableColors: List<Int>): Int? {
        if (availableColors.isEmpty()) return null
        return availableColors.random()
    }


    fun getInitialColor(): Int? {
        if (initialColor == null && colorsMap.isNotEmpty()) {
            val randomKey = colorsMap.keys.random()
            initialColor = colorsMap[randomKey]
            return initialColor
        }
        return initialColor
    }

    fun resetColors(): MutableMap<String, Int> {
        return colorsMap.toMutableMap()
    }
}

package com.example.myapplication

import android.graphics.Color

class Settings {
    // private val defTaskMaxLength: Int = 35 // Use range. Max = 35. Min = 10?
    // val defTaskColor
    // val defBackgroundColor

    // Task entry
    private val defTaskSelectedBGColor: Int = Color.BLACK
    private val defTaskSelectedTextColor: Int = Color.WHITE
    var taskSelectedBGColor: Int = Color.BLUE
    var taskSelectedTextColor: Int = Color.WHITE

    // Calendar values
    private val defCalendarRange: Int = 30
    var calendarRange: Int = defCalendarRange
    // var taskMaxLength: Int = defTaskMaxLength

    fun load() {

    }

    fun setDefault() {
        calendarRange = defCalendarRange

        taskSelectedBGColor = defTaskSelectedBGColor
        taskSelectedTextColor = defTaskSelectedTextColor
    }
}
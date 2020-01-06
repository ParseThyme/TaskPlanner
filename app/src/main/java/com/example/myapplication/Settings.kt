package com.example.myapplication

class Settings {
    // private val defTaskMaxLength: Int = 35 // Use range. Max = 35. Min = 10?

    // Task entry
    private val defTaskHighlightColor: String = "#FFFFE600"   // Yellow
    var taskHighlightColor: String = "#FFFFE600"

    // Calendar values
    private val defCalendarRange: Int = 30
    var calendarRange: Int = defCalendarRange
    // var taskMaxLength: Int = defTaskMaxLength

    fun load() {

    }

    fun setDefault() {
        calendarRange = defCalendarRange

        taskHighlightColor = defTaskHighlightColor
    }
}
package com.example.myapplication

class Settings {
    // Default values
    private val defCalendarRange: Int = 30
    // private val defTaskMaxLength: Int = 35 // Use range. Max = 35. Min = 10?
    // val defTaskColor
    // val defBackgroundColor

    var calendarRange: Int = defCalendarRange
    // var taskMaxLength: Int = defTaskMaxLength

    fun load() {

    }

    fun setDefault() {
        calendarRange = defCalendarRange
    }
}
package com.example.myapplication

class Settings {
    // Tasks
    private val defTaskHighlightColor: String = "#FFFFE600"   // Yellow
    val taskMaxLength: Int = 60
    var taskHighlightColor: String = "#FFFFE600"
    var taskBaseColor: String = "#00000000"

    // Calendar values
    private val defCalendarRange: Int = 30
    var calendarRange: Int = defCalendarRange

    fun load() {

    }

    fun setDefault() {
        calendarRange = defCalendarRange

        taskHighlightColor = defTaskHighlightColor
    }
}
package com.example.myapplication

class Settings {
    val taskMaxLength: Int = 60

    // Task entry
    private val defTaskHighlightColor: String = "#FFFFE600"   // Yellow
    var taskHighlightColor: String = "#FFFFE600"
    var taskBaseColor: String = "#00000000"

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
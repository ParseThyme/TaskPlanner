package com.example.myapplication

import java.text.SimpleDateFormat

// ########## Hardcoded values (unmodified by app) ##########
// Debug, remove at end
const val validateInput = true

// SharedPreferences (Saving/Loading data)
const val spName: String = "SavedData"
const val spTaskGroupList = "TaskGroupList"

// Activity.main
const val mainTitle = "My Task List"
// Add new task formats + variables
// Link: https://developer.android.com/reference/kotlin/android/icu/text/SimpleDateFormat
val dayFormat = SimpleDateFormat("d")
val idFormat = SimpleDateFormat("yyyyMMdd")

var minDate: Long = 0
var maxDate: Long = 0

// ########## App settings ##########
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
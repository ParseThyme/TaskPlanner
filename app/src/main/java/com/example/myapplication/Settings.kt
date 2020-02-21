package com.example.myapplication

import android.graphics.Color
import android.view.View
import java.text.SimpleDateFormat

// ########## Hardcoded values (unmodified by app) ##########
// SharedPreferences (Saving/Loading data)
const val spName: String = "SavedData"
const val spTaskGroupList = "TaskGroupList"

// Activity.main
const val mainTitle = "My Task List"
const val defaultTimeMsg = "Set Time"

// Add new task formats + variables
// Link: https://developer.android.com/reference/kotlin/android/icu/text/SimpleDateFormat
val dayFormat = SimpleDateFormat("d")
val idFormat = SimpleDateFormat("yyyyMMdd")

var minDate: Long = 0
var maxDate: Long = 0

// Time and Duration of tasks
var durationMax:Int = 180   // 3 hours, 180 minutes

// ########## App settings ##########
class Settings {
    // Unused
    // val taskMaxLength: Int = 60

    // Tasks
    private val defHighlightColor: String = "#FFFFE600"   // Yellow
    var highlightColor: String = "#FFFFE600"
    var taskBaseColor: String = "#00000000"

    // Calendar values
    private val defMaxDays: Int = 30
    var maxDays: Int = defMaxDays

    fun load() {

    }

    fun setDefault() {
        maxDays = defMaxDays

        highlightColor = defHighlightColor
    }
}

fun View.applyBackgroundColor(color: String) { setBackgroundColor(Color.parseColor(color)) }
fun View.applyBackgroundColor(color: Int) { setBackgroundColor(color) }
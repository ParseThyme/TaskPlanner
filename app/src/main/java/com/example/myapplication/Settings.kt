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

// Time and Duration of tasks
var durationMax:Int = 180   // 3 hours, 180 minutes

/* Settings set as a singleton class, we only need one instance of it and we want to make it
 * globally accessible
 * https://blog.mindorks.com/how-to-create-a-singleton-class-in-kotlin
 */

// ########## App settings ##########
object Settings {
    // init { }

    // Tasks
    private const val defHighlightColor: String = "#FFFFE600"   // Yellow
    var highlightColor: String = "#FFFFE600"
    var taskBaseColor: String = "#00000000"

    // Calendar values
    private const val defMaxDays: Int = 28
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
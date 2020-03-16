package com.example.myapplication.utility

import android.graphics.Color
import android.view.View

// ########## App settings ##########
object Settings {
    var deleteOldDates: Boolean = false

    // Popup Windows
    var tagRowSize = 8

    // Tasks
    // Groups
    var gridSpacing = 5          // In dp
    var linearSpacing = 15
    // Time
    var durationMax:Int = 480   // 8 hours, 480 minutes
    // Coloration
    private const val defHighlightColor: String = "#FFFFE600"   // Yellow
    var highlightColor: String = "#FFFFE600"
    var taskBaseColor: String = "#00000000"

    // Calendar values
    private const val defMaxDays: Int = 28
    var maxDays: Int = defMaxDays

    fun load() {

    }

    fun setDefault() {
        maxDays =
            defMaxDays

        highlightColor =
            defHighlightColor
    }
}
package com.example.myapplication.utility

// ########## App settings ##########
object Settings {
    var deleteOldDates: Boolean = false

    // Popup Windows
    var tagRowSize = 6

    var mainLayout = ViewLayout.LINEAR

    // Tasks
    // Groups
    var gridSpacing = 10          // In dp
    var linearSpacing = 15
    // Time
    var durationMax: Int = 480   // 8 hours, 480 minutes
    private const val defTimeDelta: Int = 5
    var timeDelta: Int = defTimeDelta
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
        maxDays = defMaxDays
        highlightColor = defHighlightColor
        timeDelta = defTimeDelta
    }

    // ####################
    // Layout
    // ####################
    fun switchLayout() {
        mainLayout = when(mainLayout) {
            // Switch to opposite layout
            ViewLayout.LINEAR -> ViewLayout.GRID
            ViewLayout.GRID   -> ViewLayout.LINEAR
        }
    }

    // ####################
    // Tasks
    // ####################
    fun updateTimeDelta() : String {
        when (timeDelta) {
            5 -> timeDelta = 10
            10 -> timeDelta = 15
            15 -> timeDelta = 30
            30 -> timeDelta = 60
            60 -> timeDelta = 5
        }
        return timeDeltaAsString()
    }
    fun timeDeltaAsString() : String {
        // Replace string with 1h if 60 minutes, otherwise append on m for minute values
        var result:String = timeDelta.toString()
        if (timeDelta == 60) result = "1H"
        else result += "M"
        return result
    }
}

enum class Mode { START, ADD, SELECTION }
enum class ViewLayout { LINEAR, GRID }
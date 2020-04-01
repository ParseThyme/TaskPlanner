package com.example.myapplication.utility

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapters.TaskGroupAdapter

// ########## App settings ##########
object Settings {
    var deleteOldDates: Boolean = false

    // Popup Windows
    var tagRowSize = 6

    // Tasks
    // Time
    var durationMax: Int = 480   // 8 hours, 480 minutes
    private const val defTimeDelta: Int = 5
    var timeDelta: Int = defTimeDelta
    // Coloration
    private const val defHighlightColor: String = "#FFFFE600"   // Yellow
    var highlightColor: String = "#FFFFE600"
    var taskBaseColor: String = "#00000000"

    // Date / Calendar values
    private const val defMaxDays: Int = 56
    var maxDays: Int = defMaxDays

    // Layout
    private lateinit var parentRV: RecyclerView
    private lateinit var linearLayout: LinearLayoutManager
    private lateinit var gridLayout: GridLayoutManager

    private const val gridSpacing = 10          // In dp
    private const val linearSpacing = 15
    private const val gridSpanCount = 2

    private val linearLayoutDecoration = TaskListDecoration(1, linearSpacing, true, 0)
    private val gridLayoutDecoration = TaskListDecoration(gridSpanCount, gridSpacing, true, 0)

    var mainLayout = ViewLayout.LINEAR

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
    fun setLayout(toggle: Boolean = true) {
        if (toggle) {
            mainLayout = when (mainLayout) {
                ViewLayout.LINEAR -> ViewLayout.GRID
                ViewLayout.GRID -> ViewLayout.LINEAR
            }
        }

        // Apply specified layout
        when (mainLayout) {
            ViewLayout.LINEAR -> {
                parentRV.apply {
                    layoutManager = linearLayout
                    // Remove previous decoration and replace it
                    if (itemDecorationCount > 0) removeItemDecorationAt(0)
                    addItemDecoration(linearLayoutDecoration)
                }
            }
            ViewLayout.GRID -> {
                parentRV.apply {
                    layoutManager = gridLayout
                    if (itemDecorationCount > 0) removeItemDecorationAt(0)
                    addItemDecoration(gridLayoutDecoration)
                }
            }
        }
    }
    fun initMainLayout(recyclerView: RecyclerView, taskGroupAdapter: TaskGroupAdapter) {
        // Initialize grid/linear layout here (so then its not remade every time it's toggled)
        parentRV = recyclerView
        linearLayout = LinearLayoutManager(parentRV.context)
        gridLayout = GridLayoutManager(parentRV.context, gridSpanCount, GridLayoutManager.VERTICAL, false)

        parentRV.apply {
            setLayout(false)
            adapter = taskGroupAdapter
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
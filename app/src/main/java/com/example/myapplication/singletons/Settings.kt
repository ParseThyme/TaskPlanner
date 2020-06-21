package com.example.myapplication.singletons

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.myapplication.ViewLayout
import com.example.myapplication.adapters.TaskGroupAdapter
import com.example.myapplication.recyclerviewdecoration.GridLayoutDecoration
import com.example.myapplication.recyclerviewdecoration.LinearLayoutDecoration
import kotlinx.android.synthetic.main.main_activity_view.*
import java.util.*

// ########## App settings ##########
object Settings {
    var deleteOldDates: Boolean = false

    // Popup Windows
    var tagRowSize = 8

    // Tasks
    // Time
    var durationMax: Int = 480   // 8 hours == 480 minutes
    const val defTimeDelta: Int = 30
    var timeDelta: Int = defTimeDelta
    // Coloration
    private const val defHighlightColor: String = "#FFFFE600"   // Yellow
    var highlightColor: String = "#FFFFE600"
    var taskBaseColor: String = "#00000000"

    // Date / Calendar values
    private const val defMaxMonths: Int = 4         // How many months to display (this month + x)
    var maxMonths: Int = defMaxMonths
    var startOfWeek: Int = Calendar.MONDAY

    // Layout
    private lateinit var parentRV: RecyclerView
    private lateinit var linearLayout: LinearLayoutManager
    private lateinit var gridLayout: StaggeredGridLayoutManager

    private const val gridSpanSize = 2
    private const val gridSpacing = 10          // In dp
    private const val linearSpacing = 15

    private val linearDecoration = LinearLayoutDecoration(linearSpacing)
    private val gridDecoration = GridLayoutDecoration(
        gridSpacing,
        gridSpanSize
    )

    var mainLayout = ViewLayout.LINEAR

    fun init(loadedLayout: ViewLayout, loadedDelta: Int) {
        mainLayout = loadedLayout
        timeDelta = loadedDelta
    }

    // Reset values
    fun resetTimeDelta() { timeDelta = defTimeDelta }

    // ####################
    // Layout
    // ####################
    fun layoutAsBoolean() : Boolean {
        return when (mainLayout) {
            ViewLayout.LINEAR -> false
            ViewLayout.GRID -> true
        }
    }
    fun toggleLayout(context: Context) {
        mainLayout = when (mainLayout) {
            ViewLayout.LINEAR -> ViewLayout.GRID
            ViewLayout.GRID -> ViewLayout.LINEAR
        }
        setLayout()
        SaveData.saveLayout(context)
    }
    private fun setLayout() {
        // Apply specified layout
        when (mainLayout) {
            ViewLayout.LINEAR -> {
                parentRV.apply {
                    layoutManager = linearLayout
                    // Remove previous decoration and replace it
                    if (itemDecorationCount > 0) removeItemDecorationAt(0)
                    addItemDecoration(linearDecoration)
                }
            }
            ViewLayout.GRID -> {
                parentRV.apply {
                    layoutManager = gridLayout
                    if (itemDecorationCount > 0) removeItemDecorationAt(0)
                    addItemDecoration(gridDecoration)
                }
            }
        }
    }
    fun initMainLayout(recyclerView: RecyclerView, taskGroupAdapter: TaskGroupAdapter) {
        // Initialize grid/linear layout here (so then its not remade every time it's toggled)
        parentRV = recyclerView
        linearLayout = LinearLayoutManager(parentRV.context)                                // A. Linear layout
        gridLayout = StaggeredGridLayoutManager(gridSpanSize, GridLayoutManager.VERTICAL)   // B. Grid layout

        // Apply starting layout
        parentRV.apply {
            setLayout()
            adapter = taskGroupAdapter
        }
    }
    fun usingGridLayout() : Boolean { return (mainLayout == ViewLayout.GRID) }

    // ####################
    // Tasks
    // ####################
    fun updateTimeDelta(increment: Boolean = true) : String {
        when (increment) {
            // Increase delta
            true -> {
                timeDelta = when (timeDelta) {
                    5 -> 15
                    15 -> 30
                    30 -> 60
                    else -> 5       // 60 -> 5
                }
            }
            // Decrease delta
            false -> {
                timeDelta = when (timeDelta) {
                    5 -> 60
                    15 -> 5
                    30 -> 15
                    else -> 30      // 60 -> 30
                }
            }
        }
        return timeDeltaAsString()
    }
    fun timeDeltaAsString() : String {
        // Replace string with 1h if 60 minutes, otherwise append on m for minute values
        var result:String = timeDelta.toString()
        // 5m, 30m, 1h
        result = when (timeDelta) {
            60 -> "1h"
            else -> "${result}m"
        }
        return result
    }
}
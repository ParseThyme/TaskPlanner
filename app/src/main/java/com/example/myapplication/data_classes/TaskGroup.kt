package com.example.myapplication.data_classes

import android.util.Log
import android.view.View
import com.example.myapplication.adapters.TaskGroupAdapter
import kotlin.collections.ArrayList

// ########## Data Type ##########
data class TaskGroup (
    val date: TaskDate = TaskDate(),
    val taskList: ArrayList<Task> = arrayListOf(),

    // When tasks selected
    var numSelected: Int = 0,
    // Toggle state (expanded/collapsed)
    var state: ViewState = ViewState.EXPANDED
)

fun TaskGroup.deleteSelected(): Boolean {
    // Return value represents whether deletion performed
    when (numSelected) {
        0 -> return false                              // None selected
        taskList.size -> taskList.clear()              // All selected

        // Variable number selected
        else -> {
            // Iterate through task list to find selected
            for (index: Int in taskList.size - 1 downTo 0) {
                if (taskList[index].selected)
                    taskList.removeAt(index)
            }
        }
    }

    numSelected = 0
    return true
}

// ########## Collapsing/Expanding view state ##########
fun ViewState.isNewState(view: View): Boolean {
    if (view.visibility == View.VISIBLE && this == ViewState.EXPANDED)
        return false
    if (view.visibility == View.GONE && this == ViewState.COLLAPSED)
        return false

    return true
}

fun TaskGroup.isExpanded() : Boolean { return state == ViewState.EXPANDED }

fun TaskGroup.toggleExpandCollapse(): ViewState {
    state = if (state == ViewState.EXPANDED)
        ViewState.COLLAPSED
    else
        ViewState.EXPANDED

    return state
}

// ########## Selecting/Deselecting entire group ##########
// Select all if not all selected, otherwise deselect all
fun TaskGroup.toggleHighlight() {
    // [A]. Deselect all (All have been selected)
    if (numSelected == taskList.count()) { setHighlight(false) }
    // [B]. Select all (not all have been selected)
    else { setHighlight(true) }
}

// Override highlighting with either selectAll on or off
fun TaskGroup.setHighlight(highlight: Boolean) {
    // [A]. Select all [TRUE]
    if (highlight) {
        for (task in taskList) {
            // Break early if all tasks have been selected
            if (numSelected == taskList.count()) return

            if (!task.selected) {
                task.selected = true
                numSelected++
            }
        }
    }
    // [B]. Deselect all [FALSE]
    else  {
        for (task in taskList) {
            // Break early if all tasks have been deselected
            if (numSelected == 0) return

            if (task.selected) {
                task.selected = false
                numSelected--
            }
        }
    }
}

enum class ViewState { EXPANDED, COLLAPSED }
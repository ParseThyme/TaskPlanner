package com.example.myapplication.data_classes

import android.view.View
import com.example.myapplication.R
import kotlin.collections.ArrayList

// ########## Data Type ##########
data class TaskGroup (
    val date: TaskDate = TaskDate(),
    val taskList: ArrayList<Task> = arrayListOf(),
    // When tasks selected
    var numSelected: Int = 0,
    // Toggle state (expanded/collapsed)
    var state: Fold = Fold.OUT
)

fun TaskGroup.allSelected() : Boolean { return numSelected == taskList.count() }
fun TaskGroup.isEmpty() : Boolean { return taskList.isEmpty() }

// #######################################################
// Modifying selected group
// #######################################################
fun TaskGroup.selectedDelete(data: TaskListData) {
    // Deleting entire group
    if (numSelected == taskList.size) {
        taskList.clear()
        data.numSelected -= numSelected
        numSelected = 0
        return
    }

    for (index in taskList.size - 1 downTo 0) {
        val task: Task = taskList[index]
        // Remove selected task and update counters
        if (task.selected) {
            numSelected--
            data.numSelected--
            task.selected = false
            taskList.removeAt(index)
        }
        // Exit early when all selected have been deleted (No point continuing onwards)
        if (numSelected == 0) return
    }
}
fun TaskGroup.selectedClear(data: TaskListData, paramType: TaskParam) {
    // Takes in a form of modification as input, and applies to taskList
    // Uses same logic as TaskGroup.selectedDelete
    for (index: Int in taskList.size - 1 downTo 0) {
        val task: Task = taskList[index]
        // If task is selected, apply modification
        if (task.selected) {
            numSelected--
            data.numSelected--
            task.selected = false
            task.clear(paramType)       // Clear relevant parameter in task
        }
        // Exit early once all modifications have been applied
        if (numSelected == 0) return
    }
}
fun TaskGroup.selectedSetTag(data: TaskListData, newTag: Int) {
    // See above for logic
    for (index: Int in taskList.size - 1 downTo 0) {
        val task: Task = taskList[index]
        if (task.selected) {
            numSelected--
            data.numSelected--
            task.selected = false
            task.tag = newTag
        }
        if (numSelected == 0) return
    }
}
fun TaskGroup.selectedSetTime(data: TaskListData, newTime: TaskTime) {
    // See above for logic
    for (index: Int in taskList.size - 1 downTo 0) {
        val task: Task = taskList[index]
        if (task.selected) {
            numSelected--
            data.numSelected--
            task.selected = false
            task.time = newTime
        }
        if (numSelected == 0) return
    }
}

// #######################################################
// Selecting/Deselecting entire group
// #######################################################
fun TaskGroup.toggleSelected() {
    // Select all if not all selected, otherwise deselect all

    // [A]. Deselect all (All have been selected)
    if (allSelected()) { setSelected(false) }
    // [B]. Select all (not all have been selected)
    else { setSelected(true) }
}
fun TaskGroup.setSelected(selected: Boolean) {
    // Override highlighting with either selectAll on or off

    // [A]. Select all
    if (selected) {
        for (task in taskList) {
            // Break early if all tasks have been selected
            if (numSelected == taskList.count()) return

            if (!task.selected) {
                task.selected = true
                numSelected++
            }
        }
    }
    // [B]. Deselect all
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

// #######################################################
// Fold type (IN/OUT). Whether group is expanded/collapsed
// #######################################################
fun Fold.isNew(view: View): Boolean {
    if (view.visibility == View.VISIBLE && this == Fold.OUT)
        return false
    if (view.visibility == View.GONE && this == Fold.IN)
        return false

    return true
}
fun TaskGroup.isFoldedOut() : Boolean { return state == Fold.OUT }
fun TaskGroup.toggleFold(): Fold {
    state =
        if (state == Fold.OUT)
            Fold.IN
        else
            Fold.OUT

    return state
}

enum class Fold { OUT, IN }
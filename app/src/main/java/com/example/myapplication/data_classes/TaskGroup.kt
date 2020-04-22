package com.example.myapplication.data_classes

import android.view.View
import kotlin.collections.ArrayList

// ########## Data Type ##########
data class TaskGroup (
    // A. Assigned as standard group
    val date: TaskDate = TaskDate(),                // Date category of group
    val taskList: ArrayList<Task> = arrayListOf(),  // Child task list

    var numSelected: Int = 0,                       // Child tasks selected
    var state: Fold = Fold.OUT,                     // Toggle state (expanded/collapsed)

    // B. Assigned as a header
    val groupType: GroupType = GroupType.GROUP,
    val period: Period = Period.NA,
    val label: String = ""
)

fun TaskGroup.allSelected() : Boolean { return numSelected == taskList.count() }
fun TaskGroup.isEmpty() : Boolean { return taskList.isEmpty() }

// #######################################################
// Headers
// #######################################################
fun TaskGroup.isHeader() : Boolean { return (groupType == GroupType.HEADER) }
fun TaskGroup.createHeader() : TaskGroup {
    val headerPeriod: Period = this.date.getPeriod()
    return TaskGroup(
        TaskDate(), arrayListOf(), 0, Fold.OUT,
        GroupType.HEADER, headerPeriod, headerPeriod.asString())
}
enum class GroupType { HEADER, GROUP }

// #######################################################
// Modifying selected group
// #######################################################
fun TaskGroup.selectedDelete() {
    // Deleting entire group
    if (numSelected == taskList.size) {
        taskList.clear()
        DataTracker.numSelected -= numSelected
        numSelected = 0
        return
    }

    for (index in taskList.size - 1 downTo 0) {
        val task: Task = taskList[index]
        // Remove selected task and update counters
        if (task.selected) {
            numSelected--
            DataTracker.numSelected--
            task.selected = false
            taskList.removeAt(index)
        }
        // Exit early when all selected have been deleted (No point continuing onwards)
        if (numSelected == 0) return
    }
}
fun TaskGroup.selectedSetTag(newTag: Int) {
    // See above for logic
    for (index: Int in taskList.size - 1 downTo 0) {
        val task: Task = taskList[index]
        if (task.selected) {
            numSelected--
            DataTracker.numSelected--
            task.selected = false
            task.tag = newTag
        }
        if (numSelected == 0) return
    }
}
fun TaskGroup.selectedSetTime(newTime: TaskTime) {
    // See above for logic
    for (index: Int in taskList.size - 1 downTo 0) {
        val task: Task = taskList[index]
        if (task.selected) {
            numSelected--
            DataTracker.numSelected--
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
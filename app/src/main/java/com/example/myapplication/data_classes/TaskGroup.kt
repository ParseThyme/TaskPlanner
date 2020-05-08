package com.example.myapplication.data_classes

import android.view.View
import kotlin.collections.ArrayList

// Creating container to hold TaskGroup/GroupHeader
// https://stackoverflow.com/questions/55819584/kotlin-multiple-class-for-data-storage

// ########## Data Type ##########
data class TaskGroup (
    val date: TaskDate = TaskDate(),                // Date category of group
    val taskList: ArrayList<Task> = arrayListOf(),  // Child task list

    // Selection/Folding
    var numSelected: Int = 0,                       // Child tasks selected
    var state: Fold = Fold.OUT                      // Toggle state (expanded/collapsed)
)
data class GroupHeader(val period: Period = Period.NA)

data class GroupEntry(
    val label: String,
    val type: GroupType,
    val taskGroup: TaskGroup?,
    val header: GroupHeader?
)
enum class GroupType { HEADER, GROUP }

// #######################################################
// Headers
// #######################################################
fun headerEntry(date: TaskDate) : GroupEntry {
    val period : Period = date.getPeriod()
    return GroupEntry(period.asString(), GroupType.HEADER, null, GroupHeader(period))
}

fun GroupEntry.isHeader() : Boolean { return (type == GroupType.HEADER) }
fun GroupEntry.isGroup() : Boolean { return (type == GroupType.GROUP) }

// #######################################################
// TaskGroup
// #######################################################
fun taskGroupEntry(date: TaskDate, tasks: ArrayList<Task>) : GroupEntry {
    return GroupEntry(date.asStringShort(), GroupType.GROUP, TaskGroup(date, tasks), null)
}

// ########## Selecting/Deselecting entire group ##########
fun TaskGroup.allSelected() : Boolean { return numSelected == taskList.count() }
fun TaskGroup.selectedDelete() {
    // Deleting entire group
    if (numSelected == taskList.size) {
        taskList.clear()
        DataTracker.taskCount -= numSelected
        DataTracker.numSelected -= numSelected
        numSelected = 0
        return
    }

    for (index: Int in taskList.size - 1 downTo 0) {
        val task: Task = taskList[index]
        // Remove selected task and update counters
        if (task.selected) {
            task.selected = false
            numSelected--
            taskList.removeAt(index)
            DataTracker.numSelected--
            DataTracker.taskCount--
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
fun TaskGroup.toggleSelected() {
    // Select all if not all selected, otherwise deselect all

    // [A]. Deselect all (All have been selected)
    if (allSelected()) { setSelected(false) }
    // [B]. Select all (not all have been selected)
    else { setSelected(true) }
}

// ########## Fold (Group expanded/collapsed) ##########
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
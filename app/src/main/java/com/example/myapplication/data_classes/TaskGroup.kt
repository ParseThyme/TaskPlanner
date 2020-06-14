package com.example.myapplication.data_classes

import android.view.View
import com.example.myapplication.R
import com.example.myapplication.singletons.AppData
import kotlin.collections.ArrayList

// Creating container to hold TaskGroup/GroupHeader
// https://stackoverflow.com/questions/55819584/kotlin-multiple-class-for-data-storage

// ########## Data Type ##########
data class TaskGroup (
    val date: TaskDate = TaskDate(),                // Date category of group
    val taskList: ArrayList<Task> = arrayListOf(),  // Child task list
    val label: String,

    // Selection/Folding
    var numSelected: Int = 0,                       // Child tasks selected
    var state: Fold = Fold.OUT                      // Toggle state (expanded/collapsed)
)
data class GroupHeader(
    val week: Week = Week.PAST,
    val label: String
)

data class GroupEntry(
    val type: GroupType,
    val taskGroup: TaskGroup?,
    val header: GroupHeader?
)
enum class GroupType { HEADER, GROUP }

// #######################################################
// Headers
// #######################################################
fun headerEntry(week: Week) : GroupEntry {
    return GroupEntry(GroupType.HEADER, null, GroupHeader(week, week.asString()))
}

fun GroupEntry.isHeader() : Boolean { return (type == GroupType.HEADER) }
fun GroupEntry.isGroup() : Boolean { return (type == GroupType.GROUP) }

// #######################################################
// TaskGroup
// #######################################################
fun taskGroupEntry(date: TaskDate, tasks: ArrayList<Task>) : GroupEntry {
    return GroupEntry(GroupType.GROUP, TaskGroup(date, tasks, date.asStringShort()), null)
}

// ########## Selecting/Deselecting entire group ##########
fun TaskGroup.allSelected() : Boolean { return numSelected == taskList.count() }
fun TaskGroup.selectedDelete() {
    // Deleting entire group
    if (numSelected == taskList.size) {
        taskList.clear()
        AppData.taskCount -= numSelected
        AppData.numSelected -= numSelected
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
            AppData.numSelected--
            AppData.taskCount--
        }
        // Exit early when all selected have been deleted (No point continuing onwards)
        if (numSelected == 0) return
    }
}
fun TaskGroup.selectedSetTag(newTag: Int = R.drawable.tag_base) {
    var count: Int = numSelected  // Store counter for number of tasks selected
    for (index: Int in taskList.size - 1 downTo 0) {
        val task: Task = taskList[index]
        if (task.selected) {
            count--
            task.tag = newTag
        }
        if (count == 0) return    // Once all selected tasks handled, exit
    }
}
fun TaskGroup.selectedSetTime(newTime: TaskTime = unsetTime()) {
    // See above for logic
    var count: Int = numSelected
    for (index: Int in taskList.size - 1 downTo 0) {
        val task: Task = taskList[index]
        if (task.selected) {
            count--
            task.time = newTime
        }
        if (count == 0) return
    }
}
fun TaskGroup.selectedClear() {
    for (index: Int in taskList.size - 1 downTo 0) {
        val task: Task = taskList[index]
        if (task.selected) {
            numSelected--
            AppData.numSelected--
            task.selected = false
            task.time.unset()
            task.tag = R.drawable.tag_base
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
fun TaskGroup.collapsed() : Boolean { return state == Fold.IN }
fun TaskGroup.expanded() : Boolean { return state == Fold.OUT }
fun Fold.asBoolean() : Boolean {
    return when (this) {
        Fold.IN -> true
        Fold.OUT -> false
    }
}
fun Boolean.asFold() : Fold {
    return when (this) {
        true -> Fold.IN
        false -> Fold.OUT
    }
}
enum class Fold { OUT, IN }
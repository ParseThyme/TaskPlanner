package com.example.myapplication.data_classes

import android.view.View

// ####################
// GroupEntry -> Either GroupHeader or TaskGroup
// ####################
abstract class GroupEntry {
    abstract val label: String
    abstract val type: GroupType
}
enum class GroupType { HEADER, GROUP }
// Attempt casting to either header/taskGroup
fun GroupEntry.isHeader() : Boolean { return this is GroupHeader }
fun GroupEntry.isTaskGroup() : Boolean { return this is TaskGroup }

// ####################
// Header
// ####################
data class GroupHeader (
    val period: Period,
    override val label: String = period.asString(),    // Period label
    override val type: GroupType = GroupType.HEADER
):GroupEntry()

// ####################
// Entry
// ####################
data class TaskGroup (
  val date: TaskDate = TaskDate(),                     // Date category of group
  val taskList: ArrayList<Task> = arrayListOf(),       // Child task list
  var numSelected: Int = 0,                            // Child tasks selected
  var state: Fold = Fold.OUT,                          // Folding state state (expanded/collapsed)

  override val label: String = date.asStringShort(),   // Group label
  override val type: GroupType = GroupType.GROUP
):GroupEntry()

fun TaskGroup.allSelected() : Boolean { return numSelected == taskList.count() }
// fun TaskGroup.isEmpty() : Boolean { return taskList.isEmpty() }

// ########## Modifying selected group ##########
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

// ########## Selecting/Deselecting entire group ##########
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
/*
fun TaskGroup.getSelected() : ArrayList<Task> {
    // All selected, return entire taskList
    if (allSelected()) { return taskList }

    // Otherwise go through list and return selected
    val selected: ArrayList<Task> = arrayListOf()
    for (index: Int in taskList.size - 1 downTo 0) {
        if (taskList[index].selected) { selected.add(taskList[index]) }
    }
    return selected
}
*/

// ########## Fold (IN/OUT). Whether group is expanded/collapsed ##########
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
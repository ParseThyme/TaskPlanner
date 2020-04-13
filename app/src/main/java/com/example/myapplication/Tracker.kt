package com.example.myapplication

import com.example.myapplication.data_classes.*

object Tracker {
    // Recreated every time app started. Selected state is temporary and won't be saved on app closed.
    private var selectedTasks: ArrayList<SelectedTask> = arrayListOf()

    var numSelected: Int = 0
    var taskCount: Int = 0
    var numFoldedIn: Int = 0

    // ####################
    // Setup / Data modification
    // ####################
    // Count folded task and setup values
    fun init(taskGroupList: ArrayList<TaskGroup> = arrayListOf()) {
        if (taskGroupList.isNotEmpty()) {
            for (group: TaskGroup in taskGroupList) {
                if (!group.isFoldedOut()) numFoldedIn++     // Check group fold
                taskCount += group.taskList.size            // Setup task count
            }
        }

        // By default, none selected
        numSelected = 0
    }
    // Update values after deletion
    fun deleteSelected(deleteFunction: () -> Unit) {
        deleteFunction()
        taskCount -= numSelected
        numSelected = 0
    }
    // Clear all tracker data
    fun clearAll() {
        selectedTasks.clear()
        taskCount = 0
        numFoldedIn = 0
        numSelected = 0
    }

    // ####################
    // ToggleAll logic
    // ####################
    fun selectAll(taskGroupList: ArrayList<TaskGroup>) {
        numSelected = taskCount
        // Mark every task to be selected
        for (group: TaskGroup in taskGroupList) {
            val groupID = group.date.id             // Get Group's ID

            // If entire group hasn't been selected, select them all, otherwise skip (as already fully selected)
            if (!group.allSelected()) {
                // Create selected task using groupID and index
                for (index in 0 until group.taskList.size) {
                    selectedTasks.add(SelectedTask(groupID, index))
                }
                group.numSelected = group.taskList.size      // Set num selected to group size
            }
        }
    }
    fun deselectAll() {
        numSelected = 0
        selectedTasks.clear()
    }

    // ####################
    // Getters / Setters
    // ####################
    fun allSelected(): Boolean { return numSelected == taskCount }
    fun isSelected(task: SelectedTask) : Boolean { return selectedTasks.contains(task) }    // == True when found in array

    fun toggle(task: SelectedTask) {
        // List contains task, remove it. Otherwise add it
        if (selectedTasks.contains(task)) {
            selectedTasks.remove(task)
            numSelected--
        } else {
            selectedTasks.add(task)
            numSelected++
        }
    }
    fun getSelected() : ArrayList<SelectedTask> {
        selectedTasks.sortBy { it.pos }
        return selectedTasks
    }
}
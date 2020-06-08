package com.example.myapplication.singletons

import com.example.myapplication.data_classes.TaskDate
import com.example.myapplication.data_classes.firstDayOfWeek
import com.example.myapplication.data_classes.today

object AppData {
    var numSelected: Int = 0
    var taskCount: Int = 0
    var numFoldedIn: Int = 0

    // Track sorting of taskGroupList. Done only when app started
    var sorted: Boolean = false
    var today: TaskDate = today()
    var firstDayOfWeek: TaskDate = today().firstDayOfWeek()

    fun reset() {
        numSelected = 0
        taskCount = 0
        numFoldedIn = 0
        sorted = false
        today = today()
        firstDayOfWeek = today().firstDayOfWeek()
    }

    fun selectAll(allSelected: Boolean) {
        numSelected = when (allSelected) {
            true -> taskCount
            false -> 0
        }
    }

    fun allSelected(): Boolean { return numSelected == taskCount }
    fun allCollapsed(): Boolean { return numFoldedIn == taskCount }
    fun numSelectedMsg(): String { return "Modify Selected: $numSelected / $taskCount" }
}
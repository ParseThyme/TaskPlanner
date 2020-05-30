package com.example.myapplication.data_classes

object DataTracker {
    var numSelected: Int = 0
    var taskCount: Int = 0
    var numFoldedIn: Int = 0

    fun selectAll(allSelected: Boolean) {
        numSelected = when (allSelected) {
            true  -> taskCount
            false -> 0
        }
    }
    fun allSelected(): Boolean { return numSelected == taskCount }
    fun allCollapsed() : Boolean { return numFoldedIn == taskCount }

    fun numSelectedMsg() : String { return "Modify Selected: $numSelected / $taskCount" }
}
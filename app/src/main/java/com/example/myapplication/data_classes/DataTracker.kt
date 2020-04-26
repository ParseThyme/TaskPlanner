package com.example.myapplication.data_classes

object DataTracker {
    var numSelected: Int = 0
    var taskCount: Int = 0
    var numFoldedIn: Int = 0

    fun allSelected(): Boolean { return numSelected == taskCount }
    fun selectAll() { numSelected = taskCount }
}
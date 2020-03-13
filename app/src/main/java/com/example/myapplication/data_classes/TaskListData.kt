package com.example.myapplication.data_classes

data class TaskListData(
    var numSelected: Int = 0,
    var taskCount: Int = 0,
    var numCollapsed: Int = 0
)

fun TaskListData.allSelected(): Boolean {
    return numSelected == taskCount
}

fun TaskListData.selectAll() {
    this.numSelected = taskCount
}

fun TaskListData.deleteSelected() {
    this.taskCount -= this.numSelected
    this.numSelected = 0
}
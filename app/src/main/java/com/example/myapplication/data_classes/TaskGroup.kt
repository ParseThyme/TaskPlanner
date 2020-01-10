package com.example.myapplication.data_classes

// ########## Data Type ##########
data class TaskGroup (
    val date: String = "",
    val taskList: ArrayList<Task> = arrayListOf(),

    // Identification
    val id: Int = 0,

    // When tasks selected
    var numSelected: Int = 0,
    // Toggle state (expanded/collapsed)
    var expanded: Boolean = true
)

fun TaskGroup.isSelected() : Boolean {
    if (this.numSelected == this.taskList.size)
        return true

    return false
}

fun TaskGroup.toggleExpandCollapse(): Boolean {
    expanded = !expanded

    return expanded
}
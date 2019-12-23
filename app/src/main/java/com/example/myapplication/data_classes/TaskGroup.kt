package com.example.myapplication.data_classes

// ########## Data Type ##########
data class TaskGroup (
    val date: String = "",
    val taskList: ArrayList<Task> = arrayListOf(),

    // Identification
    val id: Int = 0,

    // When tasks selected
    var numSelected: Int = 0
)

fun groupSelected(taskGroup: TaskGroup) : Boolean {
    if (taskGroup.numSelected == taskGroup.taskList.size)
        return true

    return false
}
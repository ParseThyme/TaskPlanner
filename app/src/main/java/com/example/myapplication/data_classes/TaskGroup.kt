package com.example.myapplication.data_classes

// ########## Data Type ##########
data class TaskGroup (
    val date: String = "",
    val taskList: ArrayList<Task> = arrayListOf(),

    val id: Int = 0,

    // When tasks selected
    var numSelected: Int = 0
)
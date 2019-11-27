package com.example.myapplication.data_classes

// ########## Data Type ##########
data class TaskGroup (
    val date: String = "",
    val tasks: ArrayList<Task> = arrayListOf(),

    val id: Int = 0
)
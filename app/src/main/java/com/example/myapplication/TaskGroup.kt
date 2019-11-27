package com.example.myapplication

data class TaskGroup (
    val date: String = "",
    val tasks: ArrayList<Task> = arrayListOf(),

    val id: Int = 0
)
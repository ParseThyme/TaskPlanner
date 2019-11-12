package com.example.myapplication

data class Task(
    val id: Int,
    val desc: String,
    val date: String,

    var hideDate: Boolean = false
)
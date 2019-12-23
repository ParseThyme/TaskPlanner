package com.example.myapplication

import java.text.SimpleDateFormat

// Remove at end
const val validateInput = false

// SharedPreferences (Saving/Loading data)
const val spName: String = "SavedData"
const val spTaskGroupList = "TaskGroupList"

// Activity.main
const val mainTitle = "My Task List"
// Add new task formats + variables
// Link: https://developer.android.com/reference/java/text/SimpleDateFormat
val dateFormat = SimpleDateFormat("EEE d MMM")
val idFormat = SimpleDateFormat("yyyyMMdd")
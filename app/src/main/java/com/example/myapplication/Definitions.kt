package com.example.myapplication

import java.text.SimpleDateFormat

// Remove at end
const val validateInput = true

// SharedPreferences (Saving/Loading data)
const val spName: String = "SavedData"
const val spTaskGroupList = "TaskGroupList"

// Activity.main
const val mainTitle = "My Task List"
// Add new task formats + variables
// Link: https://developer.android.com/reference/kotlin/android/icu/text/SimpleDateFormat
val dayFormat = SimpleDateFormat("d")
val idFormat = SimpleDateFormat("yyyyMMdd")

var minDate: Long = 0
var maxDate: Long = 0
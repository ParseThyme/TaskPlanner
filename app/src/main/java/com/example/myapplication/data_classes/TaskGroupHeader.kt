package com.example.myapplication.data_classes

data class TaskGroupHeader (
    val period: Period,
    val label: String = period.asString()
) : TaskGroup()

// Check if header matches up with the date of row
fun TaskGroupHeader.correctHeader(groupRow: TaskGroupRow) : Boolean {
    // 1. Check this header's period
    return when (this.period) {
        groupRow.date.dateDiff() -> true     // 2. Get row's period, if matches up then its correct
        else -> false
    }
}
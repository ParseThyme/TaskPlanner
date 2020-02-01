package com.example.myapplication.data_classes

data class TaskTime (
    var hour: String = "0",
    var min: String = "0",
    var period: String = "0"
)

fun TaskTime.asString(): String {
    return "$hour:$min $period"
}

fun TaskTime.isValid(): Boolean {
    return hour != "0"
}

fun createDisplayedTime(t1: TaskTime, t2: TaskTime): String {
    // [Case 1]. Using blank default message. If neither valid, then this isn't reassigned
    var displayedTime: String = ""
    // [Case 2]. t1 valid entry
    if (t1.isValid()) {
        displayedTime = t1.asString()

        // [Case 3]. Both valid, append t2 (newline to separate)
        if (t2.isValid())
            displayedTime = "$displayedTime\n${t2.asString()}"
    }
    // [Case 4]. t2 valid, not t1. Use it as a replacement
    else if (t2.isValid()) { displayedTime = t2.asString() }

    return displayedTime
}
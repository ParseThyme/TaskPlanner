package com.example.myapplication.popups

import com.example.myapplication.data_classes.*
import com.example.myapplication.debugMessagePrint
import com.example.myapplication.singletons.Settings
import kotlin.collections.ArrayList

data class TaskMonth (
    val month: Int = 0,
    val days: ArrayList<TaskDate> = arrayListOf()
)

fun createMonthList(): ArrayList<TaskMonth> {
    // Using today as starting point
    var currDate: TaskDate = today()
    var monthList: ArrayList<TaskMonth> = arrayListOf()

    // Create x amount of months based on defined count
    for (monthIndex: Int in 1..Settings.maxMonths) {
        // Generate month
        // 1. Get starting day (Su..Sa)
        // 2. If doesn't start at Sunday, Generate days leading up to first Sunday of month
        debugMessagePrint("Month: ${currDate.monthLabel(false)} = ${currDate.firstDayOfMonth().dayLabel(false)}")
        // Generate days in month

        currDate = currDate.addMonths(1)
    }

    return monthList
}
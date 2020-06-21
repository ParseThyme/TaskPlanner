package com.example.myapplication.popups

import com.example.myapplication.data_classes.*
import com.example.myapplication.singletons.Settings
import kotlin.collections.ArrayList

private const val monthSize = 42        // 6x7. 6 rows, 7 days per row

fun createMonthList(): MutableMap<Int, ArrayList<TaskDate>> {
    // Using today as starting point. Move to first day of month
    var currDate: TaskDate = today()
    val monthList: MutableMap<Int, ArrayList<TaskDate>> = mutableMapOf()

    // Create x amount of months based on defined count
    for (monthIndex: Int in 0..Settings.maxMonths) {
        // Generate days for month, then move to next month
        monthList[currDate.month] = createMonthDays(currDate.month, currDate.year)
        currDate = currDate.addMonths(1)
    }

    return monthList
}

private fun createMonthDays(month: Int, year: Int): ArrayList<TaskDate> {
    var currDate: TaskDate = taskDate(1, month, year)       // Start on first day of month
    val dayList: ArrayList<TaskDate> = arrayListOf()             // List storing days in month

    // If month doesn't start on Mo, move backwards to it.
    // E.g. 1 = We. Need to move back 2 days to be at Mo
    currDate = currDate.firstDayOfWeek()

    for (dayIndex: Int in 0 until monthSize) {
        dayList.add(currDate.copy())
        currDate += 1
    }

    return dayList
}
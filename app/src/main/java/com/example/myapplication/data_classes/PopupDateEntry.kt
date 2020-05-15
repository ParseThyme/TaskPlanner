package com.example.myapplication.data_classes

import com.example.myapplication.utility.Settings

data class PopupDateEntry(
    val taskDate: TaskDate,
    var label: String = "",
    var selected: Boolean = false
)

data class PopupDateWeek(
    val week: Week = Week.PAST,
    val days: ArrayList<PopupDateEntry> = arrayListOf()
)

fun createEntries() :ArrayList<PopupDateWeek> {
    // Start date
    var currDate: TaskDate = today().firstDayOfWeek()
    val pastCount: Int = dateDiff(currDate, today())    // E.g. today = Wed. From Mon-Wed = 3
    // Date list
    val dateEntries: ArrayList<PopupDateWeek> = arrayListOf()

    // 1. Create entries for first week
    val firstWeek = PopupDateWeek(Week.THIS)
    // A. Past dates set to "-"
    for (day:Int in 0 until pastCount) {
        firstWeek.days.add(PopupDateEntry(currDate.copy(), "-"))
        currDate = currDate.addDays(1)
    }
    // B. Today + Future set to day number
    for (day:Int in pastCount until 7) {
        firstWeek.days.add(PopupDateEntry(currDate.copy(), currDate.day.toString()))
        currDate = currDate.addDays(1)
    }
    dateEntries.add(firstWeek)

    // 2. Create entries for rest of weeks
    var currWeek: Week = Week.NEXT
    for (week:Int in 2..Settings.maxWeeks) {
        val weekEntry = PopupDateWeek(currWeek)
        // Populate week with days
        for (day:Int in 0 until 7) {
            weekEntry.days.add(PopupDateEntry(currDate.copy(), currDate.day.toString()))
            currDate = currDate.addDays(1)
        }
        currWeek = currWeek.next()
        dateEntries.add(weekEntry)
    }

    return dateEntries
}
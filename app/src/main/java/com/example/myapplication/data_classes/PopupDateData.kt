package com.example.myapplication.data_classes

import android.widget.TextView
import com.example.myapplication.utility.Settings
import com.example.myapplication.utility.applyBackgroundColor
import com.example.myapplication.utility.debugMessagePrint

data class PopupDateDay(
    val taskDate: TaskDate,
    var label: String = "",
    var selected: Boolean = false
)
data class PopupDateWeek(
    val week: Week = Week.PAST,
    val days: ArrayList<PopupDateDay> = arrayListOf(),
    var month: Int = 0
)
data class SelectedPopupDateDay(
    var view: TextView? = null,
    var week: Int = 0               // Index selected view can be found in
)

fun SelectedPopupDateDay.applyBackgroundColor(color: Int)    { this.view!!.applyBackgroundColor(color) }
fun SelectedPopupDateDay.applyBackgroundColor(color: String) { this.view!!.applyBackgroundColor(color) }

class PopupDateData {
    var weeks: ArrayList<PopupDateWeek> = arrayListOf()
    var months = hashMapOf<Int, Int>()                      // [Month, Index]

    init { createEntries() }

    private fun PopupDateWeek.lastDay() : PopupDateDay { return days[6] }

    private fun createEntries() {
        // Start date
        var currDate: TaskDate = today().firstDayOfWeek()
        val pastCount: Int = dateDiff(currDate, today())    // E.g. today = Wed. From Mon-Wed = 3

        debugMessagePrint("Past count: $pastCount")

        // 1. Create entries for first week
        val firstWeek = PopupDateWeek(Week.THIS)
        // A. Past dates set to "-"
        for (day:Int in 0 until pastCount) {
            firstWeek.days.add(PopupDateDay(currDate.copy(), "-"))
            currDate = currDate.addDays(1)
        }
        // B. Today + Future set to day number
        for (day:Int in pastCount until 7) {
            firstWeek.days.add(PopupDateDay(currDate.copy(), currDate.day.toString()))
            currDate = currDate.addDays(1)
        }
        // Store week, first unique month
        firstWeek.month = firstWeek.lastDay().taskDate.month
        months[firstWeek.month] = 0
        weeks.add(firstWeek)

        // 2. Create entries for rest of weeks
        var currWeek: Week = Week.NEXT
        for (week:Int in 1 until Settings.maxWeeks) {
            val weekEntry = PopupDateWeek(currWeek)
            // Populate week with days
            for (day:Int in 0 until 7) {
                weekEntry.days.add(PopupDateDay(currDate.copy(), currDate.day.toString()))
                currDate = currDate.addDays(1)
            }
            weekEntry.month = weekEntry.lastDay().taskDate.month
            // Add instance where new month reached
            if (!months.containsKey(weekEntry.month)) months[weekEntry.month] = week
            // Add week to weeks list
            currWeek = currWeek.next()
            weeks.add(weekEntry)
        }
    }
}
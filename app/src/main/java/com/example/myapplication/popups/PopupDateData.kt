package com.example.myapplication.popups

import android.widget.TextView
import com.example.myapplication.data_classes.*
import com.example.myapplication.singletons.Settings
import com.example.myapplication.applyBackgroundColor
import com.example.myapplication.singletons.AppData

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
    var startMonth: Int = 0
    var endMonth: Int = 0

    init { createEntries() }

    private fun PopupDateWeek.lastDay() : PopupDateDay { return days[6] }

    fun getDay(day:Int, week: Int) : PopupDateDay { return weeks[week].days[day] }
    fun getWeek(week: Int) : ArrayList<PopupDateDay> { return weeks[week].days }
    fun getMonth(month: Int) : Int { return months[month]!! }

    fun sunday(week: Int): PopupDateDay { return getDay(0, week) }
    fun monday(week: Int): PopupDateDay { return getDay(1, week)  }
    fun tuesday(week: Int): PopupDateDay { return getDay(2, week)  }
    fun wednesday(week: Int): PopupDateDay { return getDay(3, week)  }
    fun thursday(week: Int): PopupDateDay { return getDay(4, week)  }
    fun friday(week: Int): PopupDateDay { return getDay(5, week)  }
    fun saturday(week: Int): PopupDateDay { return getDay(6, week)  }

    fun checkOutdated() {
        // Check first week starting from Sa, going backwards
        /* [A]. If Sa is past date, week itself is outdated
            Today = 13 (Su next week). Refresh entire list
            [Su][Mo][Tu][We][Th][Fr][Sa]
            [--][--][--][--][--][--][12]
        */
        if (saturday(0).taskDate.isPastDate()) {
            weeks.clear()
            months.clear()
            createEntries()
            AppData.firstDayOfWeek = sunday(0).taskDate
            return
        }

        /* [B]. Otherwise update past dates with "-" label, when correctly labelled "-" reached, stop and exit
            Today = 31 (Th). Need to update Tu, We to blank label
            [Su][Mo][Tu][We][Th][Fr][Sa]
            [--][--][29][30][31][01][02]
            Week is always 7 days, [0] to [6]
        */
        for (day: Int in 6 downTo 0) {
            val dayData: PopupDateDay = getDay(day, 0)
            when {
                dayData.taskDate.isPastDate() -> {

                }
                // Stop early when
                dayData.label == "-" -> return
            }
        }
    }

    private fun createEntries() {
      /*
        // Start date
        var currDate: TaskDate = today().firstDayOfWeek()
        val pastCount: Int = dateDiff(currDate, today())    // E.g. today = Wed. From Mon-Wed = 3

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

        startMonth = today().month
        endMonth = weeks[weeks.lastIndex].month
        */
    }
}
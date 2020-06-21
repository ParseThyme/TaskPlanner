package com.example.myapplication.data_classes

import com.example.myapplication.Week
import com.example.myapplication.singletons.AppData
import com.example.myapplication.millisecondsToDays
import com.example.myapplication.monthsInYear
import com.example.myapplication.singletons.Settings
import java.util.*

// ####################
// TaskDate
// ####################

data class TaskDate(
    var day: Int = 0, var month: Int = 0, var year: Int = 0,
    var id: Int = 0
)
fun taskDate(day: Int, month: Int, year: Int) : TaskDate {
    val newDate = TaskDate(day, month, year, 0)
    newDate.assignID()
    return newDate
}
fun today(): TaskDate { return Calendar.getInstance().toTaskDate() }

fun TaskDate.replace(newDate: TaskDate) {
    id = newDate.id
    day = newDate.day
    month = newDate.month
    year = newDate.year
}
fun TaskDate.assignID() {
    // Construct ID in format: [YYYY][MM][DD]
    // Month and Day needs extra 0 pre-appended for values < 10
    val monthString: String = when (month < 10) {
        true -> "0$month"
        false -> "$month"
    }
    val dayString: String = when (day < 10) {
        true -> "0$day"
        false -> "$day"
    }
    id = "$year$monthString$dayString".toInt()
}

fun TaskDate.asStringShort(): String {
    val monthLabel: String = monthLabel(true)
    val dayLabel: String = dayLabel()
    return "$dayLabel | $day $monthLabel"   // Format: Mo | 21 Feb
}
fun TaskDate.asString(): String {
    val ordinal: String = dayOrdinal()
    val monthLabel: String = monthLabel(false)
    return "$day$ordinal $monthLabel"       // Format: 21st February
}

// ####################
// Days/Weeks
// ####################

fun TaskDate.dayLabel(abbreviated: Boolean = true) : String {
    val cal: Calendar = Calendar.getInstance()
    cal.set(year, month, day)
    return cal.get(Calendar.DAY_OF_WEEK).dayLabel(abbreviated)
}
fun Int.dayLabel(abbreviated: Boolean = true): String {
    // Based on: https://developer.android.com/reference/java/util/Calendar#DAY_OF_WEEK
    val result: String = when (this) {
        Calendar.SUNDAY -> "Sunday"
        Calendar.MONDAY -> "Monday"
        Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"
        Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"
        Calendar.SATURDAY -> "Saturday"
        else -> "DAY: $this (1-7)"     // Error message if not between 1 to 7
    }

    return when (abbreviated) {
        true -> result.take(2)      // Retrieve only first two characters if abbreviated
        false -> result                 // Entire day name
    }
}

private fun TaskDate.dayOrdinal() : String {
    return when {
        // [CASE 1] Set ordinal for 11th, 12th, 13th unique cases
        (day in 11..13) -> { "th" }

        // [CASE 2] Otherwise if ending with 1 == st, 2 == nd, 3 == rd, 4-9 == th
        else -> {
            when (day % 10) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
        }
    }
}

fun TaskDate.firstDayOfWeek(): TaskDate {
    // Set calendar to specific day
    val cal: Calendar = Calendar.getInstance()
    cal.set(year, month, day)

    // Get day of week based on taskDate
    var dayOfWeek: Int = cal.get(Calendar.DAY_OF_WEEK)

    // Cases depending on day in which week starts (Settings.startOfWeek):
    // 1. Su, ends Sa: [Su][Mo][Tu][We][Th][Fr][Sa] -> [1][2][3][4][5][6][7]
    // = Leave as is, calculations work out
    // 2. Mo, ends Su: [Mo][Tu][We][Th][Fr][Sa][Su] -> [2][3][4][5][6][7][1]
    // = Treat Sunday as day [8] instead of defined day [1]
    if (Settings.startOfWeek == Calendar.MONDAY && dayOfWeek == Calendar.SUNDAY) dayOfWeek = 8

    // Starting from dayOfWeek, go backwards to get start of week
    return this.minus(dayOfWeek - Settings.startOfWeek)
}

fun TaskDate.getWeek() : Week {
    val diff: Int = dateDiff(AppData.firstDayOfWeek, this)
    // println("Date diff: (${AppData.firstDayOfWeek.asStringShort()}, ${this.asStringShort()}) = $diff")
    return when {
        diff < 0 -> Week.PAST
        diff in 0..6 -> Week.THIS
        diff in 7..13 -> Week.NEXT
        diff in 14..20 -> Week.FORTNIGHT
        else -> Week.FUTURE
    }
}

fun TaskDate.weekAsString() : String { return getWeek().asString() }
fun Week.asString() : String {
    return when (this) {
        Week.PAST -> "Past Dates"
        Week.THIS -> "This Week"
        Week.NEXT -> "Next Week"
        Week.FORTNIGHT -> "Fortnight"
        Week.FUTURE -> "Future"
    }
}
fun Week.next(loop: Boolean = false) : Week {
    return when (this) {
        Week.PAST -> Week.THIS
        Week.THIS -> Week.NEXT
        Week.NEXT -> Week.FORTNIGHT
        Week.FORTNIGHT -> Week.FUTURE
        Week.FUTURE -> {
            when (loop) {
                true -> Week.PAST           // If looping, go back to first entry
                else -> Week.FUTURE         // Not looping, stop at this point
            }
        }
    }
}

// ####################
// Months
// ####################
fun Int.getEndMonth(): Int {
    return when (this !in Calendar.JANUARY..Calendar.DECEMBER) {
        true -> {
            println("[ERROR] invalid month $this")
            -1
        }
        false -> (this + Settings.maxMonths) % monthsInYear
    }
}
fun TaskDate.addMonths(months: Int = 1) : TaskDate {
    val cal: Calendar = Calendar.getInstance()
    cal.set(year, month, day)
    cal.add(Calendar.MONTH, months)
    return cal.toTaskDate()
}

fun TaskDate.monthLabel(abbreviated: Boolean = true) : String{ return this.month.monthLabel(abbreviated) }
fun Int.monthLabel(abbreviated: Boolean = true) : String {
    // Based on: https://developer.android.com/reference/java/util/Calendar#MONTH
    val result: String = when (this) {
        Calendar.JANUARY -> "January"
        Calendar.FEBRUARY -> "February"
        Calendar.MARCH -> "March"
        Calendar.APRIL -> "April"
        Calendar.MAY -> "May"
        Calendar.JUNE -> "June"
        Calendar.JULY -> "July"
        Calendar.AUGUST -> "August"
        Calendar.SEPTEMBER -> "September"
        Calendar.OCTOBER -> "October"
        Calendar.NOVEMBER -> "November"
        Calendar.DECEMBER -> "December"
        else -> "MONTH: $this (0-11)"     // Error message if not between 0 to 11
    }
    return when (abbreviated) {
        true -> result.take(3)      // Retrieve only first three characters if abbreviated
        false -> result
    }
}

// ####################
// Date Calculations
// ####################
// Override + function. E.g. today() + 7 -> today + 7 days
operator fun TaskDate.plus(days: Int) : TaskDate {
    val cal: Calendar = Calendar.getInstance()
    cal.set(year, month, day)
    cal.add(Calendar.DATE, days)
    return cal.toTaskDate()
}
operator fun TaskDate.minus(days: Int) : TaskDate { return this.plus(-days) }

private fun Calendar.toTaskDate(): TaskDate {
    // Convert Calendar set date to TaskDate format
    return taskDate(get(Calendar.DAY_OF_MONTH), get(Calendar.MONTH), get(Calendar.YEAR))
}

fun dateDiff(from: TaskDate, to: TaskDate) : Int {
    val cal: Calendar = Calendar.getInstance()

    cal.set(from.year, from.month, from.day)
    val d1 = cal.timeInMillis
    cal.set(to.year, to.month, to.day)
    val d2 = cal.timeInMillis

    return millisecondsToDays(d2 - d1)
}

fun TaskDate.isPastDate(): Boolean { return (this.id < today().id) }
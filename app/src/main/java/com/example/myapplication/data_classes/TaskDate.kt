package com.example.myapplication.data_classes

import com.example.myapplication.Week
import com.example.myapplication.debugMessagePrint
import com.example.myapplication.singletons.AppData
import com.example.myapplication.millisecondsToDays
import com.example.myapplication.singletons.Settings
import java.text.SimpleDateFormat
import java.util.*

// Add new task formats + variables
// Link: https://developer.android.com/reference/kotlin/android/icu/text/SimpleDateFormat
val idFormat = SimpleDateFormat("yyyyMMdd")

// ####################
// TaskDate
// ####################

data class TaskDate(
    var id: Int = 0,
    var day: Int = 0, var month: Int = 0, var year: Int = 0
)
fun today(): TaskDate { return Calendar.getInstance().toTaskDate() }

fun TaskDate.replace(newDate: TaskDate) {
    id = newDate.id
    day = newDate.day
    month = newDate.month
    year = newDate.year
}
fun TaskDate.createID() {
    val cal: Calendar = Calendar.getInstance()
    cal.set(year, month, day)
    this.id = idFormat.format(cal.timeInMillis).toInt()
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
    // Get difference in days between it and StartOfWeek. Then move back to Mo by subtracting difference
    val difference: Int = Settings.startOfWeek - cal.get(Calendar.DAY_OF_WEEK)
    return this.plus(difference)

    // Using Calendar.DAY_OF_WEEK: Su = 1, Mo = 2, Tu = 3, We = 4, Th = 5, Fr = 6, Sa = 7
    // E.g. today = Fr, startOfWeek = Mo: difference = -4 (2 - 6)
}

fun TaskDate.getWeek() : Week {
    val diff: Int = dateDiff(AppData.firstDayOfWeek, this)
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
fun TaskDate.addMonths(months: Int = 1) : TaskDate {
    val cal: Calendar = Calendar.getInstance()
    cal.set(year, month, day)
    cal.add(Calendar.MONTH, months)
    return cal.toTaskDate()
}

fun TaskDate.firstDayOfMonth(): Int {
    val cal: Calendar = Calendar.getInstance()
    cal.set(year, month, 1)
    return cal.get(Calendar.DAY_OF_WEEK)
}

fun TaskDate.monthLength() : Int { return month.monthLength(year) }
fun Int.monthLength(year: Int) : Int {
    // If checking Feb, test to see if leap year
    val isLeapYear: Boolean = (year % 4 == 0)
    if (isLeapYear && this == Calendar.FEBRUARY) return 29

    return when (this) {
        Calendar.JANUARY -> 31
        Calendar.FEBRUARY -> 28
        Calendar.MARCH -> 31
        Calendar.APRIL -> 30
        Calendar.MAY -> 31
        Calendar.JUNE -> 30
        Calendar.JULY -> 31
        Calendar.AUGUST -> 31
        Calendar.SEPTEMBER -> 30
        Calendar.OCTOBER -> 31
        Calendar.NOVEMBER -> 30
        Calendar.DECEMBER -> 31
        else -> {
            debugMessagePrint("Invalid Month: $this")
            -1
        }
    }
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

private fun Calendar.toTaskDate(): TaskDate {
    // Convert Calendar set date to TaskDate. Use ID for sorting.
    // E.g. 12th Feb 2020 = 20200212 -> 2020 | 02 | 12 (YEAR|MONTH|DAY ordering)
    val id: Int = idFormat.format(timeInMillis).toInt()
    return TaskDate(id, get(Calendar.DAY_OF_MONTH), get(Calendar.MONTH), get(Calendar.YEAR))
}

fun dateDiff(from: TaskDate, to: TaskDate) : Int {
    val cal: Calendar = Calendar.getInstance()

    cal.set(from.year, from.month, from.day)
    val d1 = cal.timeInMillis
    cal.set(to.year, to.month, to.day)
    val d2 = cal.timeInMillis

    return millisecondsToDays(d2 - d1)
}
fun TaskDate.isPastDate() : Boolean { return dateDiff(today(), this) < 0 }
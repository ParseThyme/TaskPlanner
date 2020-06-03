package com.example.myapplication.data_classes

import com.example.myapplication.utility.Settings
import com.example.myapplication.utility.debugMessagePrint
import com.example.myapplication.utility.millisecondsToDays
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_WEEK
import java.util.Calendar.getInstance

// Add new task formats + variables
// Link: https://developer.android.com/reference/kotlin/android/icu/text/SimpleDateFormat
val idFormat = SimpleDateFormat("yyyyMMdd")

data class TaskDate(
    var id: Int = 0,
    var day: Int = 0, var month: Int = 0, var year: Int = 0
)

fun today(): TaskDate {
    // Get calendar and add additional days, if addedDays == 0 then defaults to today's day
    val cal: Calendar = getInstance()

    // ID, used for sorting. E.g. 12th Feb 2020 = 20200212 -> 2020 | 02 | 12 (YYYYMMDD ordering)
    val id = idFormat.format(cal.timeInMillis).toInt()

    // Used for calculating subsequent days
    val day: Int = cal.get(Calendar.DAY_OF_MONTH)
    val month: Int = cal.get(Calendar.MONTH)
    val year: Int = cal.get(Calendar.YEAR)

    return TaskDate(id, day, month, year)
}
fun TaskDate.firstDayOfWeek(): TaskDate {
    val cal: Calendar = getInstance()
    cal.set(year, month, day)                   // Set calendar to specific day
    cal.set(DAY_OF_WEEK, Calendar.SUNDAY)       // Get first day in the week of that date

    val id = idFormat.format(cal.timeInMillis).toInt()
    val day: Int = cal.get(Calendar.DAY_OF_MONTH)
    val month: Int = cal.get(Calendar.MONTH)
    val year: Int = cal.get(Calendar.YEAR)

    return TaskDate(id, day, month, year)
}

fun TaskDate.replace(newDate: TaskDate) {
    id = newDate.id
    day = newDate.day
    month = newDate.month
    year = newDate.year
}
fun TaskDate.createID() {
    val cal: Calendar = getInstance()
    cal.set(year, month, day)
    this.id = idFormat.format(cal.timeInMillis).toInt()
}
// ####################
// Date Ranges, Weeks, Months
// ####################
enum class Week { PAST, THIS, NEXT, FORTNIGHT, FUTURE }

fun TaskDate.getWeekNum() : Int { return (dateDiff(AppData.firstDayOfWeek, this)) / 7 }
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

fun Int.monthAsString(): String {
    return when (this) {
        0 -> "Jan"      1 -> "Feb"      2 -> "Mar"      3 -> "Apr"
        4 -> "May"      5 -> "Jun"      6 -> "Jul"      7 -> "Aug"
        8 -> "Sep"      9 -> "Oct"      10 -> "Nov"     11 -> "Dec"
        else -> "$this: invalid month"
    }
}

fun TaskDate.same(date: TaskDate) : Boolean { return (day == date.day && month == date.month && year == date.year) }
fun TaskDate.isPastDate() : Boolean { return dateDiff(today(), this) < 0 }
fun dateDiff(from: TaskDate, to: TaskDate) : Int {
    val cal: Calendar = getInstance()

    cal.set(from.year, from.month, from.day)
    val d1 = cal.timeInMillis
    cal.set(to.year, to.month, to.day)
    val d2 = cal.timeInMillis

    return millisecondsToDays(d2 - d1)
}

// ####################
// Labels / ToString()
// ####################
fun TaskDate.asStringShort(): String {
    val cal:Calendar = Calendar.getInstance()
    cal.set(this.year, this.month, this.day)
    val timeInMills = cal.timeInMillis
    val dayNameShort: String = SimpleDateFormat("EE").format(timeInMills).dropLast(1)
    val monthShort: String = SimpleDateFormat("MMM").format(timeInMills)

    // Mo | 1st Feb
    return "$dayNameShort | $day $monthShort"
}
fun TaskDate.asString(): String {
    // Get task's date
    val cal:Calendar = getInstance()
    cal.set(this.year, this.month, this.day)

    // Calculate timeInMills, create label
    val timeInMills = cal.timeInMillis

    val ordinal = getOrdinal(cal.get(Calendar.DAY_OF_MONTH))
    val month: String = SimpleDateFormat("MMMM").format(timeInMills)

    // 21st February
    return "$day$ordinal $month"
}
fun TaskDate.dayNameShort(): String {
    val cal:Calendar = getInstance()
    cal.set(this.year, this.month, this.day)
    val timeInMills = cal.timeInMillis

    // Mo, Tu, We, Th, Fr, Sa, Su
    return SimpleDateFormat("EE").format(timeInMills).dropLast(1)
}

// ####################
// Date addition
// ####################
// fun TaskDate.addMonths(months: Int): TaskDate { return this.addPeriod(false, months) }
fun TaskDate.addDays(days: Int): TaskDate {
    // Test adding days together, if < 28, update id and day variable. Year and Month untouched
    return when (day + days < 28) {
        true  -> TaskDate(id + days, day + days, month, year)
        false -> this.addPeriod(true, days)
    }
}

private fun TaskDate.addPeriod(days: Boolean, value: Int): TaskDate {
    val cal: Calendar = getInstance()
    cal.set(year, month, day)

    // Either add days to current date, or months. Increment/Decrement based on value
    when (days) {
        true  -> cal.add(Calendar.DATE, value)      // Add Days
        false -> cal.add(Calendar.MONTH, value)     // Add Months
    }

    // ID, used for sorting. E.g. 12th Feb 2020 = 20200212 -> 2020 | 02 | 12 (YYYYMMDD ordering)
    val id = idFormat.format(cal.timeInMillis).toInt()
    // Used for calculating subsequent days
    val day: Int = cal.get(Calendar.DAY_OF_MONTH)
    val month: Int = cal.get(Calendar.MONTH)
    val year: Int = cal.get(Calendar.YEAR)

    // Return new date
    return TaskDate(id, day, month, year)
}
private fun getOrdinal(dayNum: Int) : String {
    return when {
        // [CASE 1] Set ordinal for 11th, 12th, 13th unique cases
        (dayNum in 11..13) -> { "th" }

        // [CASE 2] Otherwise if ending with 1 == st, 2 == nd, 3 == rd, 4-9 == th
        else -> {
            when (dayNum % 10) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
        }
    }
}
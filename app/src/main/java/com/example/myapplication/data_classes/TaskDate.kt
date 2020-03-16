package com.example.myapplication.data_classes

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

// Add new task formats + variables
// Link: https://developer.android.com/reference/kotlin/android/icu/text/SimpleDateFormat
val idFormat = SimpleDateFormat("yyyyMMdd")

data class TaskDate(
    var id: Int = 0,

    var day: Int = 0,
    var month: Int = 0,
    var year: Int = 0
)

fun today(): TaskDate {
    // Get calendar and add additional days, if addedDays == 0 then defaults to today's day
    val cal = Calendar.getInstance()

    // ID, used for sorting. E.g. 12th Feb 2020 = 20200212 -> 2020 | 02 | 12 (YYYYMMDD ordering)
    val id = idFormat.format(cal.timeInMillis).toInt()

    // Used for calculating subsequent days
    val day: Int = cal.get(Calendar.DAY_OF_MONTH)
    val month: Int = cal.get(Calendar.MONTH)
    val year: Int = cal.get(Calendar.YEAR)

    return TaskDate(id, day, month, year)
}

fun TaskDate.isPastDate(): Boolean {
    val cal = Calendar.getInstance()
    val today = idFormat.format(cal.timeInMillis).toInt()

    // Compare values, if id is < today's id. Then we know that its an earlier date. E.g:
    // Today: 20200316 = 2020, March, 16
    // Date:  20200314 = 2020, March, 14
    if (id < today)
        return true

    return false
}

fun TaskDate.reassign(newDate: TaskDate) {
    id = newDate.id
    day = newDate.day
    month = newDate.month
    year = newDate.year
}

fun TaskDate.createShortLabel(): String {
    val cal:Calendar = Calendar.getInstance()
    cal.set(this.year, this.month, this.day)
    val timeInMills = cal.timeInMillis
    val dayNameShort: String = SimpleDateFormat("EE").format(timeInMills).dropLast(1)
    val monthShort: String = SimpleDateFormat("MMM").format(timeInMills)

    // Mo-1-Feb
    return "$dayNameShort-$day-$monthShort"    // E.g. Fr-21-Feb
}
fun TaskDate.createLabel(): String {
    // Get task's date
    val cal:Calendar = Calendar.getInstance()
    cal.set(this.year, this.month, this.day)

    // Calculate timeInMills, create label
    val timeInMills = cal.timeInMillis
    var label: String

    val ordinal = getOrdinal(cal.get(Calendar.DAY_OF_MONTH))
    val month: String = SimpleDateFormat("MMMM").format(timeInMills)

    // 21st February
    return "$day$ordinal $month"
}
fun TaskDate.getDayNameShort(): String {
    val cal:Calendar = Calendar.getInstance()
    cal.set(this.year, this.month, this.day)
    val timeInMills = cal.timeInMillis

    // Mo, Tu, We, Th, Fr, Sa, Su
    return SimpleDateFormat("EE").format(timeInMills).dropLast(1)
}

fun TaskDate.addMonths(addedMonths: Int): TaskDate { return this.addPeriod(false, addedMonths) }
fun TaskDate.addDays(addedDays: Int): TaskDate { return this.addPeriod(true, addedDays) }

private fun TaskDate.addPeriod(days: Boolean, value: Int): TaskDate {
    val cal = Calendar.getInstance()
    cal.set(this.year, this.month, this.day)

    // Either add days to current date, or months. Increment/Decrement based on value
    if (days) { cal.add(Calendar.DATE, value) }
    else { cal.add(Calendar.MONTH, value) }

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
    // Set ordinal for 11th, 12th, 13th unique cases
    return if (dayNum in 11..13) { "th" }
    // Otherwise if ending with 1 == st, 2 == nd, 3 == rd, 4-9 == th
    else {
        when (dayNum % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
}

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

enum class Size { Long, Med, Short }
fun TaskDate.createLabel(size: Size = Size.Long): String {
    // Get task's date
    val cal:Calendar = Calendar.getInstance()
    cal.set(this.year, this.month, this.day)

    // Calculate timeInMills, create label
    val timeInMills = cal.timeInMillis
    var label: String

    // [1]. Full length:
    if (size == Size.Long) {
        // E.g. Friday February 21st
        val ordinal = getOrdinal(cal.get(Calendar.DAY_OF_MONTH))
        val dayName: String = SimpleDateFormat("EEEE").format(timeInMills)
        val month: String = SimpleDateFormat("MMMM").format(timeInMills)
        label = "$dayName $month $day$ordinal"
    }
    else {
        // val dayNameShort: String = dayOfWeek.toString()
        val dayNameShort: String = SimpleDateFormat("EE").format(timeInMills).dropLast(1)
        val monthShort: String = SimpleDateFormat("MMM").format(timeInMills)

        // [2]. Medium:
        label =
            if (size == Size.Med) {
                "$dayNameShort-$day-$monthShort"    // E.g. Fr-21-Feb
            }
            // [3]. Short:
            else {
                "$day-$monthShort"                  // E.g. 21-Feb
            }
    }

    return label
}
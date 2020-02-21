package com.example.myapplication.data_classes

import android.util.Log
import com.example.myapplication.dayFormat
import com.example.myapplication.idFormat
import java.text.SimpleDateFormat
import java.util.*

private val daysOfWeek: ArrayList<DayOfWeek> = arrayListOf(
    DayOfWeek.Su, DayOfWeek.Mo, DayOfWeek.Tu,
    DayOfWeek.We, DayOfWeek.Th, DayOfWeek.Fr,
    DayOfWeek.Sa
)

data class TaskDate(
    var id: Int = 0,
    //var dayName: DayOfWeek = DayOfWeek.Mo,

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

fun TaskDate.addDays(addedDays: Int): TaskDate {
    Log.d("Test", "Date is: ${this.createLabel(Label.Short)}")

    val cal = Calendar.getInstance()
    cal.set(this.year, this.month, this.day)
    cal.add(Calendar.DATE, addedDays)

    // ID, used for sorting. E.g. 12th Feb 2020 = 20200212 -> 2020 | 02 | 12 (YYYYMMDD ordering)
    val id = idFormat.format(cal.timeInMillis).toInt()

    // Used for calculating subsequent days
    val day: Int = cal.get(Calendar.DAY_OF_MONTH)
    val month: Int = cal.get(Calendar.MONTH)
    val year: Int = cal.get(Calendar.YEAR)

    Log.d("Test", "Adding $addedDays days: ${this.createLabel(Label.Short)}")

    // Update old values
    return TaskDate(id, day, month, year)
}

private fun TaskDate.updateValues(id:Int = -1, day:Int = -1, month:Int = -1, year:Int = -1) {
    // All values are optional to update, do nothing if value is -1
    if (id != -1)
        this.id = id

    if (day != -1)
        this.day = day

    if (month != -1)
        this.month = month

    if (year != -1)
        this.year = year
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

enum class Label { Full, Abbreviated, Short }
fun TaskDate.createLabel(type: Label = Label.Full): String {
    // Get task's date
    val cal:Calendar = Calendar.getInstance()
    cal.set(this.year, this.month, this.day)

    // Calculate timeInMills, create label
    val timeInMills = cal.timeInMillis
    var label: String

    // [1]. Full length:
    if (type == Label.Full) {
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

        // [2]. Abbreviated:
        label =
            if (type == Label.Abbreviated) {
                "$dayNameShort-$day-$monthShort"    // E.g. Fr-21-Feb
            }
            // [3]. Short:
            else {
                "$day-$monthShort"                  // E.g. 21-Feb
            }
    }

    return label
}

enum class DayOfWeek { Su, Mo, Tu, We, Th, Fr, Sa }
// https://stackoverflow.com/questions/17006239/whats-the-best-way-to-implement-next-and-previous-on-an-enum-type
fun DayOfWeek.addDays(addedDays: Int = 1): DayOfWeek {
    // Only enable moving day forward, return same day if moving backwards
    if (addedDays < 0) return this

    // Increment day of week to next, if larger than enum size (E.g. past Sa) reset to Su
    return daysOfWeek[(this.ordinal + addedDays) % daysOfWeek.count()]
}
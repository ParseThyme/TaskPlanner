package com.example.myapplication.data_classes

import android.util.Log
import com.example.myapplication.dayFormat
import com.example.myapplication.idFormat
import java.text.SimpleDateFormat
import java.util.*

private val daysOfWeek: ArrayList<DayOfWeek> = arrayListOf(
    DayOfWeek.Su, DayOfWeek.Mo, DayOfWeek.Tu, DayOfWeek.We, DayOfWeek.Th, DayOfWeek.Fr, DayOfWeek.Sa
)

data class TaskDate(
    val label: String = "",
    val labelShort: String = "",
    val labelShortest: String = "",
    val id: Int = 0,

    val dayName: DayOfWeek = DayOfWeek.Mo
)

fun getDate(addedDays: Int = 0): TaskDate {
    // Get calendar and add additional days, if addedDays == 0 then defaults to today's day
    val cal = Calendar.getInstance()
    cal.add(Calendar.DATE, addedDays)

    // Time in mills == Long value representing date
    val timeInMills = cal.timeInMillis

    // ID, used for sorting. E.g. 12th Feb 2020 = 20200212 -> 2020 | 02 | 12 (YYYYMMDD ordering)
    val id = idFormat.format(cal.timeInMillis).toInt()

    // Calendar.DAY_OF_WEEK returns day from Sunday -> Saturday. Since from 1 - 7 we need to - 1 to match
    val dayOfWeek: DayOfWeek = daysOfWeek[cal.get(Calendar.DAY_OF_WEEK) - 1]

    // Day number and ordinal (e.g. 1st, 2nd, 3rd)
    val day: String = dayFormat.format(timeInMills)
    val ordinal = getOrdinal(cal.get(Calendar.DAY_OF_MONTH))

    // Standard length: E.g. Thursday, February
    val dayName: String = SimpleDateFormat("EEEE").format(timeInMills)
    val month: String = SimpleDateFormat("MMMM").format(timeInMills)
    val date = "$dayName $month $day$ordinal"

    // Short length:
    val dayNameShort: String = dayOfWeek.toString()
    val monthShort: String = SimpleDateFormat("MMM").format(timeInMills)
    val dateShort = "$dayNameShort-$day-$monthShort"

    // Shortest length (No day name)
    val dateShortest = "$day-$monthShort"

    return TaskDate(date, dateShort, dateShortest, id, dayOfWeek)
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

enum class DayOfWeek { Su, Mo, Tu, We, Th, Fr, Sa }
// https://stackoverflow.com/questions/17006239/whats-the-best-way-to-implement-next-and-previous-on-an-enum-type
fun DayOfWeek.addDays(addedDays: Int = 1): DayOfWeek {
    // Only enable moving day forward, return same day if moving backwards
    if (addedDays < 0) return this

    // Increment day of week to next, if larger than enum size (E.g. past Sa) reset to Su
    return daysOfWeek[(this.ordinal + addedDays) % daysOfWeek.count()]
}
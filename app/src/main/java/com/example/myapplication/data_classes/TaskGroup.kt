package com.example.myapplication.data_classes

import android.util.Log
import android.view.View
import com.example.myapplication.dayFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// ########## Data Type ##########
data class TaskGroup (
    val date: String = "",
    val taskList: ArrayList<Task> = arrayListOf(),

    // Identification
    val id: Int = 0,

    // When tasks selected
    var numSelected: Int = 0,
    // Toggle state (expanded/collapsed)
    var state: ViewState = ViewState.EXPANDED
)

// ########## Date Label ##########
fun createDateLabel(cal: Calendar, short: Boolean = false) : String{
    val timeInMills = cal.timeInMillis
    // Produce day, generally either in Monday or Mon format. We want only two characters (Mo, Tu, We, etc)
    val dayName: String
    val month: String
    val day: String = dayFormat.format(timeInMills)
    var result: String

    if (short) {
        dayName = SimpleDateFormat("EE").format(timeInMills).dropLast(1)
        month = SimpleDateFormat("MMM").format(timeInMills)
        result = "$dayName $month $day"
    } else {
        dayName = SimpleDateFormat("EEEE").format(timeInMills)
        month = SimpleDateFormat("MMMM").format(timeInMills)

        // Depending on day, add ordinals
        // https://stackoverflow.com/questions/4011075/how-do-you-format-the-day-of-the-month-to-say-11th-21st-or-23rd-ordinal
        val ordinal = addOrdinal(cal.get(Calendar.DAY_OF_MONTH))
        result = "$dayName $month $day$ordinal"
    }

    return result
}

private fun addOrdinal(dayNum: Int) : String {
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

// ########## Collapsing/Expanding view state ##########
fun ViewState.isNewState(view: View): Boolean {
    if (view.visibility == View.VISIBLE && this == ViewState.EXPANDED)
        return false
    if (view.visibility == View.GONE && this == ViewState.COLLAPSED)
        return false

    return true
}

fun TaskGroup.isExpanded() : Boolean { return state == ViewState.EXPANDED }

fun TaskGroup.toggleExpandCollapse(): ViewState {
    state = if (state == ViewState.EXPANDED)
        ViewState.COLLAPSED
    else
        ViewState.EXPANDED

    return state
}

// ########## Selecting/Deselecting entire group ##########
// Select all if not all selected, otherwise deselect all
fun TaskGroup.toggleHighlight() {
    // [A]. Deselect all (All have been selected)
    if (numSelected == taskList.count()) { setHighlight(false) }
    // [B]. Select all (not all have been selected)
    else { setHighlight(true) }
}

// Override highlighting with either selectAll on or off
fun TaskGroup.setHighlight(highlight: Boolean) {
    // [A]. Select all [TRUE]
    if (highlight) {
        for (task in taskList) {
            // Break early if all tasks have been selected
            if (numSelected == taskList.count()) return

            if (!task.selected) {
                task.selected = true
                numSelected++
            }
        }
    }
    // [B]. Deselect all [FALSE]
    else  {
        for (task in taskList) {
            // Break early if all tasks have been deselected
            if (numSelected == 0) return

            if (task.selected) {
                task.selected = false
                numSelected--
            }
        }
    }
}

enum class ViewState { EXPANDED, COLLAPSED }
package com.example.myapplication.data_classes

import android.widget.TextView
import com.example.myapplication.utility.Settings
import com.example.myapplication.utility.debugMessagePrint
import com.example.myapplication.utility.defaultTimeMsg

data class TaskTime (
    var hour: Int = 0,
    var min: Int = 0,
    var timeOfDay: String = "AM",
    var duration: Int = 0
)

fun TaskTime.isValid(): Boolean { return hour > 0 }
fun TaskTime.getOppositeTimeOfDay(): String {
    if (timeOfDay == "AM") { return "PM" }
    return "AM"
}

// ####################
// Set values
// ####################
fun TaskTime.resetValues() {
    hour = 12
    min = 0
    timeOfDay = "AM"
    duration = 0
}
fun TaskTime.clear(applyToView: TextView? = null) {
    // Optional, update textView with default time message
    if (applyToView != null) applyToView.text = defaultTimeMsg

    hour = -1
    min = 0
    timeOfDay = "AM"
    duration = 0
}

fun TaskTime.update(increment: Boolean = true) {
    var newMinutes: Int = min
    var hourDelta = 0
    var flipToD = false

    // Ensure starting hour is a valid time
    if (hour < 0) hour = 12

    if (increment) {
        newMinutes += Settings.timeDelta

        // Result is a number over 60
        if (newMinutes > 59) {
            // Calculate how many hours we need to add to time and add it
            hourDelta = newMinutes / 60
            // Ensure time between 0-60
            newMinutes %= 60
        }
    }
    else {
        newMinutes -= Settings.timeDelta

        // Result is number under 0 minutes
        if (newMinutes < 0) {
            // Ensure time in appropriate range
            newMinutes += 60
            // Calculate hours required to subtract
            hourDelta = -((newMinutes + 60) / 60)
        }
    }

    // Assuming hour has been updated, make sure result is in range
    if (hourDelta != 0) {
        val hourResult = hour + hourDelta

        when {
            // Values > 12, reset back to 1
            hourResult > 12 -> {
                hour = 1
                flipToD = true
            }
            // Values < 1, reset to 12
            hourResult < 1 -> {
                hour = 12
                flipToD = true
            }
            // Standard hour increment/decrement. Value between 1-12
            else -> hour = hourResult
        }

        if (flipToD) {
            // Flip time of day
            timeOfDay = getOppositeTimeOfDay()
        }
    }

    // Update minute value
    min = newMinutes
}

// ####################
// Creating string labels
// ####################
fun TaskTime.createStartTime(withTimeOfDay: Boolean = true): String {
    // If time is 0, return base string message
    if (hour == 0) return defaultTimeMsg

    // Create start time
    val timeAsString: String = if (hour < 0) "12:00"
                               else "$hour:${minutesAsString(min)}"

    // If time of day included, add "AM" OR "PM" to end of string
    return if (withTimeOfDay) "$timeAsString$timeOfDay"
           else timeAsString
}

fun TaskTime.createTimeWithDuration(): String {
    var displayedTime: String = this.createStartTime()

    // Check if duration allocated. If so append end time based on duration.
    if (duration > 0) {
        // Convert duration to hours and minutes
        val addedHours:Int = duration / 60
        val addedMinutes:Int = duration % 60

        // Add together hours and minutes from time and duration
        var endTimeOfDay:String = timeOfDay
        var endMin: Int = addedMinutes + min
        var endHrs: Int = addedHours + hour + (endMin/60)  // Add overflow (e.g. 70/60 = 1h10min)
        endMin %= 60                                       // Ensure minutes between 0-59

        // Check hours value, if > 12 then we swapped time of day. E.g. 12am -> 2pm
        if (endHrs > 12) {
            endHrs %= 12
            endTimeOfDay = this.getOppositeTimeOfDay()
        }

        // Append end time to currently displayed time
        val endTime = "$endHrs:${minutesAsString(endMin)}$endTimeOfDay"
        displayedTime = "$displayedTime - $endTime"
    }

    return displayedTime
}
fun TaskTime.durationToString(): String {
    // [1]. Duration as Int from 0 to 59 minutes. Return number as : followed by duration. E.g. :30
    if (duration in 0..59) {
        // Extra: If duration from 1 - 9. Add extra 0 in front
        if (duration in 1..9)
            return ":0$duration"
        return ":$duration"
    }

    // [2]. Duration 60m+. Return with hour and minutes format
    val hours = duration/60
    val minutes = duration%60

    // Create duration string with hour value
    var durationString = "$hours:00"

    // Append on minutes if > 0
    if (minutes > 0) {
        durationString =
            if (minutes in 1..9)
                "$hours:0$minutes"      // Append extra 0 for 0-9 values
            else
                "$hours:$minutes"       // Otherwise show minutes normally
    }

    return durationString
}

fun minutesAsString(minutes: Int): String {
    // If minutes are 0-9 append extra 0 in front
    if (minutes in 0..9) return "0$minutes"
    return minutes.toString()
}
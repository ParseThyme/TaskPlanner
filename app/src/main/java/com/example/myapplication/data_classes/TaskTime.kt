package com.example.myapplication.data_classes

import android.widget.TextView
import com.example.myapplication.utility.Settings
import com.example.myapplication.utility.debugMessagePrint
import com.example.myapplication.utility.defaultTimeMsg

data class TaskTime (
    var hour: Int = 0,
    var min: Int = 0,
    var timeOfDay: TimeOfDay = TimeOfDay.AM,
    var duration: Int = 0
)

fun TaskTime.isValid(): Boolean { return hour > 0 }

// ####################
// Time Of Day
// ####################
enum class TimeOfDay {AM, PM}
fun TimeOfDay.asString() : String{
    return when (this) {
        TimeOfDay.AM -> "am"
        TimeOfDay.PM -> "pm"
    }
}
fun TaskTime.getOppositeTimeOfDay(): TimeOfDay {
    return when (timeOfDay) {
        TimeOfDay.AM -> TimeOfDay.PM
        else -> TimeOfDay.AM
    }
}

// ####################
// Set values
// ####################
fun unsetTime() : TaskTime { return TaskTime(-1, 0, TimeOfDay.AM, 0)}
fun TaskTime.unset() {
    hour = -1
    min = 0
    timeOfDay = TimeOfDay.AM
    duration = 0
}
fun TaskTime.setDefault() {
    hour = 12
    min = 0
    timeOfDay = TimeOfDay.AM
    duration = 0
}

/*
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
*/

fun TaskTime.updateMin(increment: Boolean = true) {
    val minDelta = 5
    when (increment) {
        true -> {
            min += minDelta
            // Result is a number over 60, Ensure time between 0-60
            if (min > 59) min %= 60
        }
        false -> {
            min -= minDelta
            // Result is number under 0 minutes, Ensure time in appropriate range
            if (min < 0) min += 60
        }
    }
}
fun TaskTime.updateHour(increment: Boolean = true) {
    val hourDelta = 1
    var updateTimeOfDay = false

    when (increment) {
        // Increase hour. If result is >12 then loop back from 1 and update time of day
        true -> {
            hour += hourDelta
            if (hour > 12) {
                updateTimeOfDay = true
                hour %= 12
            }
        }
        // Decrease hour. If result is <1 then start from 12 and update time of day
        false -> {
            hour -= hourDelta
            if (hour < 1) {
                updateTimeOfDay = true
                hour += 12
            }
        }
    }

    if (updateTimeOfDay) timeOfDay = getOppositeTimeOfDay()
}
fun TaskTime.updateDuration(increment: Boolean = true) {
    when (increment) {
        // Increase duration
        true -> {
            duration += Settings.timeDelta
            if (duration > Settings.durationMax) duration = Settings.durationMax // Prevent overflow
        }
        // Decrease duration
        false -> {
            duration -= Settings.timeDelta
            if (duration < 0) duration = 0      // Set duration to 0 for negative values
        }
    }
}

// ####################
// Creating string labels
// ####################
fun TaskTime.startTimeLabel(withTimeOfDay: Boolean = true): String {
    // If time is <0, return base string message
    if (hour <= 0) return defaultTimeMsg

    // Create start time
    val timeAsString: String = when {
        (hour < 0) -> "12:00"
              else -> "$hour:${min.minutesAsString()}"
    }

    // If time of day included, add "AM" OR "PM" to end of string
    return when (withTimeOfDay) {
        true  -> "$timeAsString${timeOfDay.asString()}"
        false -> timeAsString
    }
}
fun TaskTime.endTimeLabel(): String {
    // No need to create end time label if duration <= 0
    if (duration <= 0) return "NA"

    // Convert duration to hours and minutes
    val addedHours:Int = duration / 60
    val addedMinutes:Int = duration % 60

    // Add together hours and minutes from time and duration
    var endTimeOfDay:String = timeOfDay.asString()
    var endMin: Int = addedMinutes + min
    var endHrs: Int = addedHours + hour + (endMin/60)  // Add overflow (e.g. 70/60 = 1h10min)
    endMin %= 60                                       // Ensure minutes between 0-59

    // Check hours value, if > 12 then we swapped time of day. E.g. 12am -> 2pm
    if (endHrs > 12) {
        endHrs %= 12
        endTimeOfDay = this.getOppositeTimeOfDay().asString()
    }

    // Append end time to currently displayed time
    return "$endHrs:${endMin.minutesAsString()}$endTimeOfDay"
}
fun TaskTime.overallTimeLabel(): String {
    // If no duration, return just start time. Otherwise create end time label and return both combined
    return if (duration <= 0)
        startTimeLabel()
    else
        "${startTimeLabel()} - ${endTimeLabel()}"
}

fun TaskTime.durationAsString(): String {
    // [1]. Duration as Int from 0 to 59 minutes.
    when (duration) {
        in 0..9   -> return "00:${duration.minutesAsString()}"
        in 10..59 -> return "00:$duration"
    }

    // [2]. Duration 60m+. Return with hour and minutes format
    val hours: Int = duration/60
    val minutes: Int = duration%60
    return "$hours:${minutes.minutesAsString()}"
}
fun Int.minutesAsString(): String {
    // If minutes are 0-9 append extra 0 in front
    return when (this) {
        in   0..9 -> "0$this"
        in 10..59 -> "$this"
             else -> "[!] $this"
    }
}
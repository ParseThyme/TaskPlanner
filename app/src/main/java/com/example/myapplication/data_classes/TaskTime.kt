package com.example.myapplication.data_classes

import android.util.Log
import com.example.myapplication.durationHourFormat
import com.example.myapplication.durationMinuteFormat

data class TaskTime (
    var hour: Int = 0,
    var min: Int = 0,
    var timeOfDay: String = "AM",
    var duration: Int = 0,
    var durationInc: Int = 5
)

fun TaskTime.asString(): String { return "$hour:${minutesAsString(min)} $timeOfDay" }
fun TaskTime.isValid(): Boolean { return hour != 0 }
fun TaskTime.getOppositeTimeOfDay(): String {
    if (timeOfDay == "AM") { return "PM" }
    return "AM"
}

fun TaskTime.resetValues() {
    hour = 0
    min = 0
    timeOfDay = "AM"
    duration = 0
    //durationInc = 5
}

fun TaskTime.createDisplayedTime(): String {
    var displayedTime = ""

    // Check if hour value is not "0" (valid time)
    if (this.isValid()) {
        // Convert it to string format
        displayedTime = this.asString()

        // Check if duration allocated. If so append end time based on duration.
        if (this.duration > 0) {
            // Convert duration to hours and minutes
            val addedHours:Int = duration / 60
            val addedMinutes:Int = duration % 60

            // Add together hours and minutes from time and duration
            var t2TimeOfDay:String = this.timeOfDay
            var t2Min: Int = addedMinutes + this.min
            var t2Hrs: Int = addedHours + this.hour + (t2Min/60)  // Add overflow (e.g. 70/60 = 1h10min)
            t2Min %= 60                                           // Ensure minutes between 0-59

            // Check hours value, if > 12 then we swapped time of day. E.g. 12am -> 2pm
            if (t2Hrs > 12) {
                t2Hrs %= 12
                t2TimeOfDay = this.getOppositeTimeOfDay()
            }

            // Append t2 to currently displayed time with newline in between
            displayedTime = "$displayedTime\n${t2Hrs}:${minutesAsString(t2Min)} $t2TimeOfDay"
        }
    }

    return displayedTime
}

fun durationAsString(durationAsInt: Int): String {
    var durationAsString: String
    // [1]. Duration as Int from 0 to 59 minutes. Return number with "m" appended to the end
    if (durationAsInt in 0..59) { durationAsString = "${durationAsInt}m" }
    // [2]. Duration from 60minutes+. Return with hour and minutes format. E.g. 2h30m
    else {
        val hours = durationAsInt/60
        val minutes = durationAsInt % 60

        durationAsString = "${hours}h"

        // Append on minutes if > 0
        if (minutes > 0)
            durationAsString = "$durationAsString${minutes}m"
    }

    return durationAsString
}

fun durationAsInt(durationAsString: String):Int {
    var durationAsInt = 0
    /* Input format will be in format:
     - 0m    = 0-9   minutes
     - 00m   = 10-59 minutes
     - 0h,   = 1-9   hours
     - 0h0m  = 1-9   hours,   0-9 minutes
     - 0h00m = 1-9   hours,   10-59 minutes
     */

    // Find values for hour and minutes, via regex
    var hour: String? = durationHourFormat.find(durationAsString)?.value
    var minute:String? = durationMinuteFormat.find(durationAsString)?.value

    // Hour value exists. Convert to minutes
    if (hour != null) {
        // Remove "h" at end, then multiply by 60 to get minutes
        val hourAsInt:Int = hour.dropLast(1).toInt()
        durationAsInt += hourAsInt * 60
    }
    // Minutes value exists
    if (minute != null) {
        val minuteAsInt = minute.dropLast(1).toInt()
        durationAsInt += minuteAsInt
    }

    return durationAsInt
}

fun minutesAsString(minutes: Int): String {
    // If minutes are 0-9 append extra 0 in front
    if (minutes in 0..9) return "0$minutes"
    return minutes.toString()
}
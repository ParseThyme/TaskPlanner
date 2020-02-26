package com.example.myapplication.data_classes

import android.util.Log
import com.example.myapplication.defaultTimeMsg

data class TaskTime (
    var hour: Int = 0,
    var min: Int = 0,
    var timeOfDay: String = "AM",
    var duration: Int = 0
)

fun TaskTime.asString(withTimeOfDay: Boolean = true): String {
    // If time is 0:00, return base string message
    if (this.hour == 0) return defaultTimeMsg

    // Create start time
    var timeAsString = "$hour:${minutesAsString(min)}"

    // If time of day included, add " AM" OR " PM" to end of string
    if (withTimeOfDay) timeAsString += " $timeOfDay"

    return timeAsString
}
fun TaskTime.isValid(): Boolean { return hour != 0 }
fun TaskTime.getOppositeTimeOfDay(): String {
    if (timeOfDay == "AM") { return "PM" }
    return "AM"
}

fun TaskTime.resetValues() {
    hour = 12
    min = 0
    timeOfDay = "AM"
    duration = 0
}

fun TaskTime.createDisplayedTime(): String {
    var displayedTime = this.asString()

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
        displayedTime = "${displayedTime}\n${t2Hrs}:${minutesAsString(t2Min)} $t2TimeOfDay"
    }

    return displayedTime
}

fun TaskTime.durationAsString(): String {
    // [1]. Duration as Int from 0 to 59 minutes. Return number with "m" appended to the end
    if (duration in 0..59) { return "${duration}m" }

    // [2]. Duration 60m+. Return with hour and minutes format
    val hours = duration/60
    val minutes = duration % 60

    // Create duration string with hour value
    var durationString = "${hours}h"

    // Append on minutes if > 0
    if (minutes > 0) durationString = "$durationString${minutes}m"

    return durationString
}

fun minutesAsString(minutes: Int): String {
    // If minutes are 0-9 append extra 0 in front
    if (minutes in 0..9) return "0$minutes"
    return minutes.toString()
}
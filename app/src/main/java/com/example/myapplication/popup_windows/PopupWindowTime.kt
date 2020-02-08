package com.example.myapplication.popup_windows

import android.content.Context
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.data_classes.durationAsInt
import com.example.myapplication.data_classes.durationAsString
import com.example.myapplication.durationMax
import kotlinx.android.synthetic.main.time_popup_window.view.*

fun Context.createTimePopup(parent: View): PopupWindow {
    val timePopup:PopupWindow = this.createPopup(R.layout.time_popup_window, parent)

    // Set behaviour for changing time period (AM/PM)
    val view:View = timePopup.contentView

    // Apply button click functionality
    // [1]. Time - Set time
    view.txtTimeOfDay.setOnClickListener { view.txtTimeOfDay.changeTimeOfDay() }
    view.btnHourUp.setOnClickListener { view.txtHour.updateHour() }
    view.btnHourDown.setOnClickListener { view.txtHour.updateHour(false) }
    view.btnMinuteUp.setOnClickListener{
        view.txtMinute.updateMinutes(view.txtIncDelta.text.toString()) }
    view.btnMinuteDown.setOnClickListener {
        view.txtMinute.updateMinutes(view.txtIncDelta.text.toString(),false) }

    // Delta value, affects increment for minutes and duration
    view.txtIncDelta.setOnClickListener { view.txtIncDelta.updateDelta() }

    // [2]. Time - Duration
    view.btnDurationUp.setOnClickListener {
        view.txtDuration.updateDuration(view.txtIncDelta.text.toString()) }
    view.btnDurationDown.setOnClickListener {
        view.txtDuration.updateDuration(view.txtIncDelta.text.toString(), false) }

    // [3]. Time - Reset
    view.btnResetTime.setOnClickListener {
        view.txtHour.text = "0"
        view.txtMinute.text = "00"
        view.txtTimeOfDay.text = "AM"

        view.txtDuration.text = "0m"
        view.txtIncDelta.text = "5m"
    }

    return timePopup
}

private fun TextView.updateHour(increment: Boolean = true) {
    var hour: Int = text.toString().toInt()
    if (increment) { hour++ }
    else { hour-- }

    // Ensure values are always between 0-12
    if (hour < 0)  { hour = 12 }
    else if (hour > 12) { hour = 0 }

    text = hour.toString()
}
private fun TextView.updateMinutes(deltaString: String, increment: Boolean = true) {
    val delta = durationAsInt(deltaString)
    var currentMinutes = text.toString()
    // For values between 00 to 09. Drop the initial '0' character
    if (currentMinutes[0] == '0') { currentMinutes = currentMinutes.drop(0) }

    var minutes: Int = currentMinutes.toInt()

    // Increment/decrement
    if (increment) {
        minutes += delta
        // If value over 60, override with remainder. E.g. 75 % 60 = 15
        if (minutes >= 60) { minutes %= 60 }
    }
    else {
        minutes -= delta
        // Ensure value > 0, wrap around to 60. E.g. 1:05 - 15m = 1:50
        if (minutes < 0) { minutes += 60 }
    }

    // If value < 10, we append extra 0 to the front
    var display = if (minutes < 10)
        "0$minutes"
    else
        minutes.toString()

    this.text = display
}
private fun TextView.changeTimeOfDay() {
    text = when (text) {
        "AM" -> "PM"
        else -> "AM"
    }
}

private fun TextView.updateDuration(deltaString: String, increment: Boolean = true) {
    val duration = durationAsInt(this.text.toString())
    val delta = durationAsInt(deltaString)
    var newDuration: Int
    var durationDisplayed: String

    // Increment
    if (increment) {
        newDuration = duration + delta
        // Ensure duration < cap
        if (newDuration > durationMax) { newDuration = 0 }
    }
    // Decrement
    else {
        newDuration = duration - delta
        // Ensure duration >= 0
        if (newDuration < 0) { newDuration =
            durationMax
        }
    }

    // Convert duration value to String format
    durationDisplayed = durationAsString(newDuration)

    this.text = durationDisplayed
}
private fun TextView.updateDelta() {
    // Increment values possible are: 5, 10, 15, 30 minute increment/decrements
    val newVal = when(this.text) {
        "5m" -> "10m"
        "10m" -> "15m"
        "15m" -> "30m"
        else -> "5m"
    }
    this.text = newVal
}
package com.example.myapplication

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import com.example.myapplication.data_classes.durationAsInt
import com.example.myapplication.data_classes.durationAsString
import kotlinx.android.synthetic.main.time_popup_window.view.*

// https://stackoverflow.com/questions/23516247/how-change-position-of-popup-menu-on-android-overflow-button
fun Context.createTagPopupWindow(parent: View): PopupWindow {
    return createPopupWindow(R.layout.tag_popup_window, parent)
}

fun Context.createTimePopupWindow(parent: View): PopupWindow {
    // Adding additional offset as popup window is 2 rows in height (by default anchored to top row)
    val timeWindow: PopupWindow = createPopupWindow(R.layout.time_popup_window, parent)

    // Set behaviour for changing time period (AM/PM)
    val view:View = timeWindow.contentView

    // Apply button click functionality
    // [1]. Time - Set time
    view.txtTimeOfDay.setOnClickListener { view.txtTimeOfDay.changeTimeOfDay() }
    view.btnHourUp.setOnClickListener { view.txtHour.updateHour() }
    view.btnHourDown.setOnClickListener { view.txtHour.updateHour(false) }
    view.btnMinuteUp.setOnClickListener { view.txtMinute.updateMinutes() }
    view.btnMinuteDown.setOnClickListener { view.txtMinute.updateMinutes(false) }

    // [2]. Time - Duration
    view.txtDurationInc.setOnClickListener { view.txtDurationInc.changeDurationDelta() }
    view.btnDurationUp.setOnClickListener {
        val delta:String = view.txtDurationInc.text.toString()
        view.txtDuration.updateDuration(delta)
    }
    view.btnDurationDown.setOnClickListener {
        val delta:String = view.txtDurationInc.text.toString()
        view.txtDuration.updateDuration(delta, false)
    }

    // [3]. Time - Reset
    view.btnResetTime.setOnClickListener {
        view.txtHour.text = "0"
        view.txtMinute.text = "00"
        view.txtTimeOfDay.text = "AM"

        view.txtDuration.text = "0m"
        view.txtDurationInc.text = "5m"
    }

    return timeWindow
}

private fun Context.createPopupWindow(layout: Int, parent: View): PopupWindow {
    val inflater: LayoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view: View = inflater.inflate(layout, null)
    val window = PopupWindow(this)

    // Apply parameters to window
    window.apply{
        isFocusable = true
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        contentView = view
    }

    /* Link: https://stackoverflow.com/questions/4303525/change-gravity-of-popupwindow
    - Get measurements of content window (gives access to measuredHeight/measuredWidth)
    */
    window.contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    val padding = 5
    val xOffset:Int = -padding                                      // Default right of parent, shift left
    val yOffset:Int = -window.contentView.measuredHeight - padding  // Default below parent, shift up by height

    // Create window at specified parent
    window.showAsDropDown(parent, xOffset, yOffset)

    return window
}

// ########## OnClick behaviours ##########

// Time window
private fun TextView.updateHour(increment: Boolean = true) {
    var hour: Int = text.toString().toInt()
    if (increment) { hour++ }
    else { hour-- }

    // Ensure values are always between 0-12
    if (hour < 0)  { hour = 12 }
    else if (hour > 12) { hour = 0 }

    text = hour.toString()
}
private fun TextView.updateMinutes(increment: Boolean = true, delta: Int = 5) {
    var currentMinutes = text.toString()
    // For values between 00 to 09. Drop the initial '0' character
    if (currentMinutes[0] == '0') { currentMinutes = currentMinutes.drop(0) }

    var minutes: Int = currentMinutes.toInt()

    // Increment/decrement
    if (increment) { minutes += delta }
    else { minutes -= delta }

    // Ensure values are between 00 - 59
    if (minutes > 59) { minutes = 0 }
    else if (minutes < 0) { minutes = 60 - delta }

    // If value < 10, we append extra 0 to the front
    var display = if (minutes < 10)
        "0$minutes"
    else
        minutes.toString()

    text = display
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
        if (newDuration > durationMax) { newDuration = durationMax }
    }
    // Decrement
    else {
        newDuration = duration - delta
        // Ensure duration >= 0
        if (newDuration < 0) { newDuration = 0 }
    }

    // Convert duration value to String format
    durationDisplayed = durationAsString(newDuration)

    this.text = durationDisplayed
}
private fun TextView.changeDurationDelta() {
    // Increment values possible are: 5, 10, 15, 30, 60 minute increment/decrements
    val newVal = when(this.text) {
        "5m" -> "10m"
        "10m" -> "15m"
        "15m" -> "30m"
        "30m" -> "1h"
        else -> "5m"
    }
    this.text = newVal
}
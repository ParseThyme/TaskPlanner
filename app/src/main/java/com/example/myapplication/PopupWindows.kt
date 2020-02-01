package com.example.myapplication

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
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
    // Time1
    view.txtT1Period.setOnClickListener { view.txtT1Period.changeTimeOfDay() }
    view.btnT1HourUp.setOnClickListener { view.txtT1Hour.updateHour() }
    view.btnT1HourDown.setOnClickListener { view.txtT1Hour.updateHour(false) }
    view.btnT1MinUp.setOnClickListener { view.txtT1Min.updateMinutes() }
    view.btnT1MinDown.setOnClickListener { view.txtT1Min.updateMinutes(false) }
    // Time2
    view.txtT2Period.setOnClickListener { view.txtT2Period.changeTimeOfDay() }
    view.btnT2HourUp.setOnClickListener { view.txtT2Hour.updateHour() }
    view.btnT2HourDown.setOnClickListener { view.txtT2Hour.updateHour(false) }
    view.btnT2MinUp.setOnClickListener { view.txtT2Min.updateMinutes() }
    view.btnT2MinDown.setOnClickListener { view.txtT2Min.updateMinutes(false) }

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
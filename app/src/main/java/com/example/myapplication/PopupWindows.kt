package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.PopupWindow
import kotlinx.android.synthetic.main.time_popup_window.view.*

// https://stackoverflow.com/questions/23516247/how-change-position-of-popup-menu-on-android-overflow-button
fun Context.createTagPopupWindow(parent: View, yOffset: Int = 0): PopupWindow {
    return createPopupWindow(R.layout.tag_popup_window, parent, yOffset)
}

fun Context.createTimePopupWindow(parent: View, yOffset: Int = 0): PopupWindow {
    // Adding additional offset as popup window is 2 rows in height (by default anchored to top row)
    val timeWindow: PopupWindow = createPopupWindow(R.layout.time_popup_window, parent, yOffset + parent.height)

    // Set behaviour for changing time period (AM/PM)
    val view:View = timeWindow.contentView

    view.btnStartTimeOfDay.setOnClickListener { view.btnStartTimeOfDay.changeTimeOfDay() }
    view.btnEndTimeOfDay.setOnClickListener { view.btnEndTimeOfDay.changeTimeOfDay() }

    return timeWindow
}

private fun Context.createPopupWindow(layout: Int, parent: View, yOffset: Int = 0): PopupWindow {
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

    // Create window at specified parent
    window.showAsDropDown(parent)

    // Reposition window to be above parent, set -1 to ignore width and height modification
    val location = IntArray(2)
    parent.getLocationInWindow(location)

    val xOff = 5
    val yOff = 5 + yOffset

    // By default creates a bit to right and bottom of parent
    val xPos:Int = location[0] - xOff      // Align left
    val yPos:Int = (location[1] - yOff)    // Up

    window.update(xPos, yPos,-1, -1)

    return window
}

// ########## OnClick behaviours ##########

private fun Button.changeTimeOfDay() {
    when (this.text) {
        "AM" -> this.text = "PM"
        else -> this.text = "AM"
    }
}
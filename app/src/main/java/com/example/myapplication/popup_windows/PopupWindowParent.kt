package com.example.myapplication.popup_windows

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow

// https://stackoverflow.com/questions/23516247/how-change-position-of-popup-menu-on-android-overflow-button
fun Context.createPopup(layout: Int, parent: View): PopupWindow {
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
package com.example.myapplication.popup_windows

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow

abstract class PopupWindowParent {
    // https://stackoverflow.com/questions/23516247/how-change-position-of-popup-menu-on-android-overflow-button
    fun create(context: Context, layout: Int): PopupWindow {
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(layout, null)
        val window = PopupWindow(context)

        // Apply parameters to window
        window.apply{
            isFocusable = true
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            contentView = view
        }

        return window
    }

    // Create window and immediately show it
    fun createAndShow(context: Context, layout: Int, parent: View) : PopupWindow {
        val window:PopupWindow = create(context, layout)
        window.show(parent)
        return window
    }
}

// Manually show window at desired point in time
fun PopupWindow.show(parent: View) {
    /* Link: https://stackoverflow.com/questions/4303525/change-gravity-of-popupwindow
    - Get measurements of content window (gives access to measuredHeight/measuredWidth)
    */
    this.contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    val padding = 5
    val xOffset:Int = -padding                                    // Default right of parent, shift left
    val yOffset:Int = -this.contentView.measuredHeight - padding  // Default below parent, shift up by height

    // Create window at specified parent
    this.showAsDropDown(parent, xOffset, yOffset)
}
package com.example.myapplication.popup_windows

import android.content.Context
import android.graphics.Point
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import com.example.myapplication.getDisplaySize

abstract class PopupParent {
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
    fun createAndShow(context: Context, layout: Int, parent: View, anchor: Anchor = Anchor.Above) : PopupWindow {
        val window:PopupWindow = create(context, layout)
        window.show(parent, anchor)
        return window
    }
}

// Manually show window at desired point in time
fun PopupWindow.show(parent: View, anchor: Anchor = Anchor.Above) {
    // Link: https://stackoverflow.com/questions/4303525/change-gravity-of-popupwindow

    // Get created window measurements to determine shifts
    this.contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

    val displaySize: Point = parent.getDisplaySize()
    val viewSize = Point(this.contentView.measuredWidth, this.contentView.measuredHeight)
    val padding = 5
    val xOffset:Int = (displaySize.x - viewSize.x) / 2      // X positioning. Ensure at center of parent

    // Determining Y Positioning depending on anchor. Place above or below
    val yOffset = when(anchor) {
        Anchor.Above -> -viewSize.y - padding - parent.height
        Anchor.Below -> 0
    }

    // Show popup with offsets applied
    this.showAsDropDown(parent, xOffset, yOffset)
}

enum class Anchor { Above, Below }
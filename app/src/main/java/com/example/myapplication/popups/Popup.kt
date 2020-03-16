package com.example.myapplication.popups

import android.content.Context
import android.graphics.Point
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import com.example.myapplication.utility.Keyboard

abstract class Popup {
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

    // Manually show window at desired point in time
    fun PopupWindow.show(parent: View, anchor: Anchor = Anchor.Above) {
        // Link: https://stackoverflow.com/questions/4303525/change-gravity-of-popupwindow

        // 1. If keyboard open, then place popup on top of keyboard and stretch to match
        if (Keyboard.visible) {
            apply {
                // Match keyboard width and height
                width = Keyboard.width
                height = Keyboard.height
                // Ensure popup overlaps keyboard
                inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
            }

            // Move popup to bottom of screen
            this.showAtLocation(parent, Gravity.CENTER, 0, Keyboard.maxHeight)
        }
        // 2. Otherwise shift position accordingly above/below parent
        else {
            // Get created window measurements to determine shifts
            this.contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

            val viewSize = Point(this.contentView.measuredWidth, this.contentView.measuredHeight)
            val yPadding = 10       // Add y spacing between parent and popup
            val xPadding = 5        // By default, positioned a bit right of parent, this should offset it

            // X positioning. Ensure at center of parent
            val xOffset: Int = (parent.width - viewSize.x) / 2 - xPadding

            // Determining Y Positioning depending on anchor. Place above or below
            val yOffset = when (anchor) {
                Anchor.Above -> -viewSize.y - yPadding - parent.height
                Anchor.Below -> 0
            }

            // Show popup with offsets applied
            this.showAsDropDown(parent, xOffset, yOffset)
        }
    }

    enum class Anchor { Above, Below }
}
package com.example.myapplication.popup_windows

import android.content.Context
import android.graphics.Point
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import com.example.myapplication.getDisplaySize
import com.example.myapplication.getScreenLocation


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
    - By default, y-pos is directly below parent

    1. Get distance from bottom of screen to parent
    2. Distance top of screen to parent == parent's coordinates
    3. Whichever distance is shorter, use relevant anchoring
    */

    val location: Point = parent.getScreenLocation()
    val displaySize: Point = parent.getDisplaySize()
    val distance: Int = displaySize.y - location.y       // From bottom of screen to parent

    // Get created window measurements to determine shifts
    this.contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    val viewSize = Point(this.contentView.measuredWidth, this.contentView.measuredHeight)

    val padding = 10
    val xOffset:Int = (displaySize.x - viewSize.x) / 2      // X positioning. Ensure at center of parent
    var yOffset = 0                                         // Y positioning. Place either above or below

    // Determining Y Positioning
    // If View closer to bottom of screen, place above parent. Otherwise no offset and set to default below
    if (distance < location.y) { yOffset = -viewSize.y - padding - parent.height }

    // Show popup with offsets applied
    this.showAsDropDown(parent, xOffset, yOffset)
}
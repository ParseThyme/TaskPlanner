package com.example.myapplication.popup_windows

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
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
    fun createAndShow(context: Context, layout: Int, parent: View, anchor: Anchor = Anchor.BottomLeft) : PopupWindow {
        val window:PopupWindow = create(context, layout)
        window.show(parent)
        return window
    }
}

// Manually show window at desired point in time
fun PopupWindow.show(parent: View, anchor: Anchor = Anchor.BottomLeft) {
    /* Link: https://stackoverflow.com/questions/4303525/change-gravity-of-popupwindow
    - Get measurements of content window (gives access to measuredHeight/measuredWidth)
    - By default, y-pos is directly below parent
    */

    // Get distance from bottom of screen to parent
    // Get distance from top of screen to parent
    // Whichever distance is shorter, use relevant anchoring

    val location = IntArray(2)
    parent.getLocationOnScreen(location)
    Log.d("Test", "Location: (${location[0]}, ${location[1]})")

    // X positioning, untouched
    val padding = 5
    val xOffset:Int = -padding              // Default a bit right of parent, shift left

    // Y positioning, dependant on anchoring
    var yOffset = 0

    yOffset = when (anchor) {
        Anchor.BottomLeft -> {
            // Get window measurements to determine upwards shift then assign it to yOffset
            this.contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            -this.contentView.measuredHeight - padding
        }
        Anchor.TopLeft -> -this.height - padding
    }

    // Create window at specified parent
    this.showAsDropDown(parent, xOffset, yOffset)
}

// Where popup is placed relative to parent
enum class Anchor { BottomLeft, TopLeft }

// Bottom left anchoring. Popup's bottom left matching parent's top left
 /* Matching bottom left corner [X]
  | Parent + Popup | Added |
  |           222  |  222  |
  |  111      222  |  333  |
  |  X11      X22  |  X33  |
 */

// TopLeft anchoring. Popup's top left matching parent's top left
 /* Matching top left corner [Y]
   | Parent + Popup | Added |
   |  Y11      Y22  |  Y33  |
   |  111      222  |  333  |
   |           222  |  222  |
 */

// If popup same size as parent, regardless of anchoring, it wouldn't matter
/* Example: Top Left
  | Parent + Popup | Added |
  |  X11      X22  |  X33  |
  |  111      222  |  333  |
  Example: Bot Left
  | Parent + Popup | Added |
  |  111      222  |  333  |
  |  X11      X22  |  X33  |
 */
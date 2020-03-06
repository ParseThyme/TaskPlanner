package com.example.myapplication

import android.app.Activity
import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import android.view.View

object Keyboard {
    var visible: Boolean = false
    var height: Int  = 0
    var maxHeight: Int = 0
    var width: Int = 0

    fun setup(activity: Activity) {
        val mainActivity: Activity = MainActivity()
        val root = activity.window.decorView

        // Size of view when keyboard is closed. Basically maximum height it reaches and width (shouldn't change)
        val occupiedSpace = root.getOccupiedSpace()
        maxHeight = occupiedSpace.y
        width = occupiedSpace.x

        // https://proandroiddev.com/how-to-detect-if-the-android-keyboard-is-open-269b255a90f5
        root.viewTreeObserver.addOnGlobalLayoutListener {
            // How much space is being occupied currently. Same as maxHeight if keyboard is not open
            val occupiedSpace = root.getOccupiedSpace()

            // Size of keyboard equal to max height - occupied space. If 0 we know keyboard is not open.
            height = maxHeight - occupiedSpace.y

            // Keyboard size is generally over 200. If value is less then keyboard isn't open
            visible = height > 200
        }
    }
}

fun View.getOccupiedSpace() : Point {
    // How much space currently the view takes up
    // https://stackoverflow.com/questions/22589322/what-does-top-left-right-and-bottom-mean-in-android-rect-object
    val r = Rect()  // Has top, bottom, left, right values. We only care about right/bottom in this case
    this.getWindowVisibleDisplayFrame(r)
    return Point(r.right, r.bottom)
}
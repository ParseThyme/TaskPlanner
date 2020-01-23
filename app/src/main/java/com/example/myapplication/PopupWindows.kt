package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow

// https://stackoverflow.com/questions/23516247/how-change-position-of-popup-menu-on-android-overflow-button
fun Activity.createTagPopupWindow(parent: View) : PopupWindow {
    val inflater: LayoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view: View = inflater.inflate(R.layout.tag_popup_window, null)
    val window = PopupWindow(this)
    val padding:Int = 5

    // Apply parameters to window
    window.apply{
        isFocusable = true
        width = WindowManager.LayoutParams.WRAP_CONTENT
        // Match parent's size and account for padding below and above
        height = parent.height + parent.paddingTop
        contentView = view
    }

    // By default creates underneath parent, we need to add offsets
    val xOffset:Int = -padding                                      // Right
    val yOffset:Int = -(parent.height + padding) // Up

    // Create window at specified parent
    window.showAsDropDown(parent, xOffset, yOffset)

    return window
}

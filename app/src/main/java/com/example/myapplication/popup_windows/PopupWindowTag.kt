package com.example.myapplication.popup_windows

import android.content.Context
import android.view.View
import android.widget.PopupWindow
import com.example.myapplication.R

fun Context.createTagPopup(parent: View): PopupWindow {
    return this.createPopup(R.layout.tag_popup_window, parent)
}
package com.example.myapplication.popup_windows.edit

import android.content.Context
import android.view.View
import android.widget.PopupWindow
import com.example.myapplication.R
import com.example.myapplication.popup_windows.Anchor
import com.example.myapplication.popup_windows.PopupTag
import com.example.myapplication.popup_windows.PopupWindowParent
import kotlinx.android.synthetic.main.task_edit_popup_window.view.*

class PopupEdit(private val parent: View, private val context: Context) : PopupWindowParent() {

    fun create(anchor: Anchor = Anchor.BottomLeft): PopupWindow {
        val window:PopupWindow = createAndShow(context, R.layout.popup_date, parent, anchor)
        val view:View = window.contentView

        return window
    }
}
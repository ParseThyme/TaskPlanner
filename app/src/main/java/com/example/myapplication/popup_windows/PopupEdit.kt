package com.example.myapplication.popup_windows

import android.content.Context
import android.view.View
import android.widget.PopupWindow
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.popup_windows.Anchor
import com.example.myapplication.popup_windows.PopupTag
import com.example.myapplication.popup_windows.PopupParent
import kotlinx.android.synthetic.main.popup_edit.view.*

class PopupEdit(private val parent: View,
                private val context: Context)
    : PopupParent()
{

    fun create(anchor: Anchor = Anchor.Above): PopupWindow {
        val window:PopupWindow = createAndShow(context, R.layout.popup_edit, parent, anchor)
        val view:View = window.contentView

        return window
    }
}
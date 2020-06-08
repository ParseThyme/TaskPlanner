package com.example.myapplication.singletons

import android.content.Context
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import com.example.myapplication.data_classes.*
import com.example.myapplication.popups.PopupDate
import com.example.myapplication.popups.PopupTag
import com.example.myapplication.popups.PopupTime

object PopupManager {
    private lateinit var time: PopupTime
    private lateinit var tag: PopupTag
    private lateinit var date: PopupDate

    fun setup(tagList: ArrayList<Int>) {
        time =
            PopupTime()
        tag =
            PopupTag(tagList)
        date =
            PopupDate()
    }

    // Create relevant popups
    fun timeEdit(attachTo: View, modify: TextView?, context: Context, edited: TaskTime) : PopupWindow {
        return time.create(attachTo, modify, context, edited)
    }

    fun tagEdit(attachTo: View, modify: TextView?, context: Context,
                edited: Task) : PopupWindow {
        return tag.create(attachTo, modify, context, edited)
    }

    fun dateEdit(attachTo: View, modify: TextView?, context: Context,
                 edited: TaskDate) : PopupWindow {
        return date.create(attachTo, modify, context, edited)
    }
}
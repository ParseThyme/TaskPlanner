package com.example.myapplication.popup_windows

import android.content.Context
import android.util.Log
import android.view.View
import com.example.myapplication.data_classes.*

object PopupManager {

    private lateinit var time: PopupTime
    private lateinit var tag: PopupTag
    private lateinit var date: PopupDate

    fun setup(tagList: ArrayList<TaskTag>) {
        time = PopupTime()
        tag = PopupTag(tagList)
        date = PopupDate()
    }

    // Create relevant popups
    fun timePopup(attachTo: View, modify: View, context: Context, edited: Task) {
        time.create(attachTo, modify, context, edited)
    }
    fun tagPopup(attachTo: View, modify: View, context: Context, edited: Task) {
        tag.create(attachTo, modify, context, edited)
    }
    fun datePopup(attachTo: View, modify: View, context: Context, edited: TaskDate) {
        date.create(attachTo, modify, context, edited)
    }
}
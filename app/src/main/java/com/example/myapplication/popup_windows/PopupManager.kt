package com.example.myapplication.popup_windows

import android.content.Context
import android.util.Log
import android.view.View
import com.example.myapplication.data_classes.*

object PopupManager {
    val time: PopupTime = PopupTime()
    val tag: PopupTag = PopupTag()
    val date: PopupDate = PopupDate()

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

enum class PopupType { Time, Tag, Date }
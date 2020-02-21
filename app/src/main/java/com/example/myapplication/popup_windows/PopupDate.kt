package com.example.myapplication.popup_windows

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.data_classes.*
import kotlinx.android.synthetic.main.popup_date.view.*
import kotlinx.android.synthetic.main.popup_time.view.*
import kotlinx.android.synthetic.main.popup_time.view.txtDate

class PopupDate(private val parent: Button, private val context: Context) : PopupWindowParent() {
    // By default set to today's date
    var setDate: TaskDate = getDate()
        private set

    private var date: TaskDate = TaskDate()
    private var dateDelta: DateDelta = DateDelta.D

    fun create() : PopupWindow {
        val window:PopupWindow = createAndShow(context, R.layout.popup_date, parent)
        val view:View = window.contentView

        // Copy over most recent date
        date = setDate.copy()

        // Apply values based on set date
        view.txtDate.text = date.labelShort

        // onClick behaviours:
        view.txtDeltaDays.setOnClickListener {
            view.txtDeltaDays.updateDelta()
        }

        return window
    }

    private fun PopupWindow.resetValues() {
        val view:View = this.contentView

    }

    private fun TextView.updateDate(increment: Boolean = true) {

    }

    private fun TextView.updateDelta() {
        dateDelta = dateDelta.next()
        this.text = dateDelta.toString()
    }
}

enum class DateDelta { D, W, M }
fun DateDelta.next(): DateDelta {
    return when (this) {
        DateDelta.D -> DateDelta.W
        DateDelta.W -> DateDelta.M
        else -> DateDelta.D
    }
}
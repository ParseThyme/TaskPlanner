package com.example.myapplication.popup_windows

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.Settings
import com.example.myapplication.data_classes.*
import kotlinx.android.synthetic.main.popup_date.view.*
import kotlinx.android.synthetic.main.popup_time.view.*
import kotlinx.android.synthetic.main.popup_time.view.txtDate

class PopupDate(private val parent: Button,
                private val settings: Settings,
                private val context: Context)
    : PopupWindowParent()
{
    // Today's date and cap date
    private val today: TaskDate = today()
    lateinit var endDate: TaskDate

    // By default set to today's date
    var setDate: TaskDate = today
        private set

    private var date: TaskDate = TaskDate()
    private var dateDelta: DateDelta = DateDelta.D

    fun create() : PopupWindow {
        val window:PopupWindow = createAndShow(context, R.layout.popup_date, parent)
        val view:View = window.contentView

        // Copy over most recent date
        date = setDate.copy()

        date.addDays(1)

        // Get date cap
        endDate = today.addDays(settings.maxDays)
        // Check whether we need to hide any of the up/down buttons. Do so if matching today or end date
        when (date.id) {
            today.id -> view.btnUpDate.visibility = View.INVISIBLE
            endDate.id -> view.btnDownDate.visibility = View.INVISIBLE
        }

        // Apply values based on set date
        view.txtDate.text = date.createLabel(Label.Abbreviated)

        // onClick behaviours:
        view.txtDeltaDays.setOnClickListener { view.txtDeltaDays.updateDelta() }

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
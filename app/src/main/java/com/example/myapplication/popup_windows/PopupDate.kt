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
import kotlinx.android.synthetic.main.popup_time.view.txtDate

class PopupDate : PopupParent() {
    // Today's date and cap date
    private lateinit var today: TaskDate
    lateinit var endDate: TaskDate

    private var date: TaskDate = TaskDate()
    private var dateDelta: DateDelta = DateDelta.D

    fun create(attachTo: View, modify: View, context: Context, edited: TaskDate, anchor: Anchor = Anchor.Above) : PopupWindow {
        val window:PopupWindow = createAndShow(context, R.layout.popup_date, attachTo, anchor)
        val view:View = window.contentView

        // Get today's date. Should update when clock strikes 12:00AM for new day
        today = today()

        // Copy over most recent date (or edited date) and get date cap
        date = edited.copy()
        endDate = today.addDays(Settings.maxDays)

        // Refresh in case 12:00 midnight
        if (date.id < today.id) { date = today() }
        // Check whether we need to hide any of the up/down buttons. Do so if matching today or end date
        if (date.id == today.id) { view.btnDecDate.visibility = View.INVISIBLE }
        else if (date.id == endDate.id) { view.btnIncDate.visibility = View.INVISIBLE }

        // Apply values based on set date
        view.txtDate.text = date.createLabel(Size.Med)

        // onClick behaviours:
        view.btnDecDate.setOnClickListener { view.txtDate.updateDate(view.btnIncDate, view.btnDecDate, false) }
        view.btnIncDate.setOnClickListener { view.txtDate.updateDate(view.btnIncDate, view.btnDecDate) }

        view.txtDeltaDays.setOnClickListener { view.txtDeltaDays.updateDelta() }

        view.btnResetDate.setOnClickListener {
            dateDelta = DateDelta.D
            date = today

            view.btnDecDate.visibility = View.INVISIBLE
            view.txtDate.text = date.createLabel(Size.Med)
            view.txtDeltaDays.text = "D"
        }
        view.btnApplyDate.setOnClickListener {
            window.dismiss()
            edited.reassign(date)
            (modify as TextView).text = date.createLabel(Size.Med)
        }

        return window
    }

    private fun TextView.updateDate(incBtn: View, decBtn: View, increment: Boolean = true) {
        // Change delta value based on days. For month, we use another function for calculation
        val deltaPeriod = when (dateDelta) {
            DateDelta.D -> 1
            DateDelta.W -> 7
            else -> 1
        }

        // Move date forward
        if (increment) {
            // If date previously at start date, re-enable decrementing date
            if (date.id == today.id) { decBtn.visibility = View.VISIBLE }

            date = if (dateDelta == DateDelta.M)
                 date.addMonths(deltaPeriod)       // Adding month
            else date.addDays(deltaPeriod)         // Adding days

            // Check if result is over end date
            if (date.id >= endDate.id) {
                date = endDate.copy()
                incBtn.visibility = View.INVISIBLE
            }
        }
        // Move date backwards
        else {
            // If date previously at end date, re-enable incrementing date
            if (date.id == endDate.id) { incBtn.visibility = View.VISIBLE }

            date = if (dateDelta == DateDelta.M)
                 date.addMonths(-deltaPeriod)       // Subtracting month
            else date.addDays(-deltaPeriod)         // Subtracting days

            // Check if result is before today, if so reset to today and disable decrementing
            if (date.id <= today.id) {
                date = today.copy()
                decBtn.visibility = View.INVISIBLE
            }
        }

        // Assign new value
        this.text = date.createLabel(Size.Med)
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
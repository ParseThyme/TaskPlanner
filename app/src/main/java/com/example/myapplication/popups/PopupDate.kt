package com.example.myapplication.popups

import android.content.Context
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.utility.Settings
import com.example.myapplication.data_classes.*
import kotlinx.android.synthetic.main.popup_date.view.*

class PopupDate : Popup() {
    // Today's date and cap date
    private lateinit var today: TaskDate
    private lateinit var endDate: TaskDate
    // Monday of end week. If date is later than it. We know date belongs to final week
    private lateinit var monEndWeek: TaskDate

    private var date: TaskDate = TaskDate()

    fun create(attachTo: View, modify: TextView?, context: Context, edited: TaskDate, anchor: Anchor = Anchor.Above) : PopupWindow {
        val window:PopupWindow = createAndShow(context, R.layout.popup_date, attachTo, anchor)
        val view:View = window.contentView

        // Get tracked dates. today, endDate, and monEndWeek
        today = today()
        endDate = today.addDays(Settings.maxDays)
        monEndWeek = endDate.firstDayOfWeek()

        // Copy over most recent date (or edited date)
        date = edited.copy()

        // Refresh in case 12:00 midnight
        if (date.id < today.id) { date = today() }

        view.txtSelectedDate.text = date.asStringShort()    // Selected date

        // Generate week given selected date
        val week: ArrayList<TaskDate> = date.getWeek()

        // Check whether we need to hide forward/backward buttons. Do so if matching this week or last week
        when {
            date.thisWeek() -> {
                view.btnPeriodPrev.visibility = View.INVISIBLE
                // Hide past dates. When marked as 0, display as "-" and do nothing when clicked
                val pastDays: Int = dateDiff(today.firstDayOfWeek(), today)
                for (index: Int in 0 until pastDays) { week[index].day = 0 }
            }
            date.finalWeek() -> {
                view.btnPeriodNext.visibility = View.INVISIBLE
            }
        }

        // onClick:
        // Mo - Su cells
        view.mon.setupClickableDay(week[0], view.txtMon, view.txtSelectedDate)
        view.tue.setupClickableDay(week[1], view.txtTue, view.txtSelectedDate)
        view.wed.setupClickableDay(week[2], view.txtWed, view.txtSelectedDate)
        view.thu.setupClickableDay(week[3], view.txtThu, view.txtSelectedDate)
        view.fri.setupClickableDay(week[4], view.txtFri, view.txtSelectedDate)
        view.sat.setupClickableDay(week[5], view.txtSat, view.txtSelectedDate)
        view.sun.setupClickableDay(week[6], view.txtSun, view.txtSelectedDate)

        // Apply changes
        view.btnApplyDate.setOnClickListener {
            edited.replace(date)
            modify?.text = date.asStringShort()
            window.dismiss()
        }

        // ToDo:
        // Reset button
        // Adjust months to properly display

        // Val startMonth = week[0].month
        // Val endMonth = week[6].month
        // If same only show start month

        /*
        view.btnDecDate.setOnClickListener { view.txtDate.updateDate(view.btnIncDate, view.btnDecDate, false) }
        view.btnIncDate.setOnClickListener { view.txtDate.updateDate(view.btnIncDate, view.btnDecDate) }

        view.txtDeltaDays.setOnClickListener { view.txtDeltaDays.updateDelta() }

        view.btnResetDate.setOnClickListener {
            dateDelta = DateDelta.D
            date = today

            view.btnDecDate.visibility = View.INVISIBLE
            view.txtDate.text = date.asStringShort()
            view.txtDeltaDays.text = "D"
        }
        */

        return window
    }

    private fun View.setupClickableDay(taskDate: TaskDate, display: TextView, selectedDate: TextView) {
        // 1. Set initial display
        display.text = taskDate.dayNumString()

        // Don't add a click listener if past date (display shown as "-")
        if (taskDate.day != 0) {
            // ClickListener = override selected date and update its display
            this.setOnClickListener {
                date = taskDate
                selectedDate.text = taskDate.asStringShort()

                // ToDo: Highlight this yellow
            }
        }
    }

    private fun TaskDate.thisWeek() : Boolean { return getPeriod() == Period.THIS_WEEK }
    private fun TaskDate.finalWeek() : Boolean { return id >= monEndWeek.id }
}
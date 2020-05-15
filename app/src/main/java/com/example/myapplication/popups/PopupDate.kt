package com.example.myapplication.popups

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.utility.Settings
import com.example.myapplication.data_classes.*
import com.example.myapplication.utility.applyBackgroundColor
import kotlinx.android.synthetic.main.popup_date.view.*

class PopupDate : Popup() {
    // Today's date
    private lateinit var today: TaskDate
    // Most recently chosen date
    private var date: TaskDate = TaskDate()
    private lateinit var selectedCell: TextView // Highlighted cell
    // List of selectable dates
    private var dateList: ArrayList<PopupDateWeek> = createEntries()
    // Final week (since we start from 0, need to -1)
    private val lastWeek = Settings.maxWeeks - 1
    // Tracker for current week
    private var currWeek: Int = 0

    fun create(attachTo: View, modify: TextView?, context: Context, edited: TaskDate, anchor: Anchor = Anchor.Above) : PopupWindow {
        val window:PopupWindow = createAndShow(context, R.layout.popup_date, attachTo, anchor)
        val view:View = window.contentView

        // 1. Get tracked dates. today
        today = today()
        // Copy over most recent date (or edited date)
        date = edited.copy()

        // Refresh in case 12:00 midnight
        if (date.id < today.id) { date = today() }

        // 2. Setup Arrays
        // [A]. Cell underneath, text updated
        val updatedTextViews: ArrayList<TextView> = arrayListOf(
            view.txtMon, view.txtTue, view.txtWed, view.txtThu, view.txtFri, view.txtSat, view.txtSun)

        // 3. Setup text displays
        view.txtSelectedDate.text = date.asStringShort()         // Selected date
        view.txtWeek.text = date.getWeek().asString()            // Selected week
        setDayLabels(updatedTextViews, dateList[currWeek].days)  // Days in week

        // 4. Check whether we need to hide forward/backward buttons. Do so if matching this week or last week
        when (currWeek) {
            0 ->        view.btnWeekPrev.visibility = View.INVISIBLE   // This week, hide backwards
            lastWeek -> view.btnWeekNext.visibility = View.INVISIBLE   // Final week, hide forwards
        }

        // 5. Setup onClick behaviours
        // [A]. Mo - Su cells
        view.mon.setupDayClickListener(0, view.txtMon, view.txtSelectedDate)
        view.tue.setupDayClickListener(1, view.txtTue, view.txtSelectedDate)
        view.wed.setupDayClickListener(2, view.txtWed, view.txtSelectedDate)
        view.thu.setupDayClickListener(3, view.txtThu, view.txtSelectedDate)
        view.fri.setupDayClickListener(4, view.txtFri, view.txtSelectedDate)
        view.sat.setupDayClickListener(5, view.txtSat, view.txtSelectedDate)
        view.sun.setupDayClickListener(6, view.txtSun, view.txtSelectedDate)

        // [B].
        view.btnWeekNext.setOnClickListener {
            // Increment week and update labels
            currWeek++
            view.txtWeek.updateWeek()
            setDayLabels(updatedTextViews, dateList[currWeek].days)

            // Check result
            when (currWeek) {
                // Week: 0 -> 1 (Enable back button)
                1 -> view.btnWeekPrev.visibility = View.VISIBLE
                // Week: secondLast -> last (disable forward button)
                lastWeek -> view.btnWeekNext.visibility = View.INVISIBLE
            }
        }
        view.btnWeekPrev.setOnClickListener {
            // Decrement week and update labels
            currWeek--
            view.txtWeek.updateWeek()
            setDayLabels(updatedTextViews, dateList[currWeek].days)

            // Check result
            when (currWeek) {
                // Week: 1 -> 0 (Disable back button)
                0 -> view.btnWeekPrev.visibility = View.INVISIBLE
                // Week: last -> secondLast (enable forward button)
                (lastWeek - 1) -> view.btnWeekNext.visibility = View.VISIBLE
            }
        }

        // [C]. Apply changes
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

    private fun View.setupDayClickListener(dayIndex: Int, highlightedView: TextView, updatedTextView: TextView) {
        setOnClickListener {
            val clickedDay: PopupDateEntry = dateList[currWeek].days[dayIndex]
            // Two cases where nothing is done:
            // [A]. Not selectable, label == "-".
            // [B]. Already selected, highlighted and displayed above
            if (clickedDay.label != "-" && (clickedDay.taskDate.id != date.id)) {
                // Update label and selected date
                date = clickedDay.taskDate
                updatedTextView.text = clickedDay.taskDate.asStringShort()

                // Clear previous highlight, switch then highlight new cell view
                selectedCell.applyBackgroundColor(Color.WHITE)
                selectedCell = highlightedView
                selectedCell.applyBackgroundColor(Settings.highlightColor)
            }
        }
    }
    private fun setDayLabels(textViews: ArrayList<TextView>, dayData: ArrayList<PopupDateEntry>) {
        for (index: Int in 0..textViews.lastIndex) {
            textViews[index].text = dayData[index].label

            // Highlight date yellow if selected
            if (dayData[index].taskDate.id == date.id) {
                selectedCell = textViews[index]
                selectedCell.applyBackgroundColor(Settings.highlightColor)
            }
        }
    }

    private fun TextView.updateWeek() {
        this.text = dateList[currWeek].week.asString()      // Update text display
        selectedCell.applyBackgroundColor(Color.WHITE)      // Unhighlight selectedCell
    }
}
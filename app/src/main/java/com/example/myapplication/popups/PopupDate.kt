package com.example.myapplication.popups

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.utility.Settings
import com.example.myapplication.data_classes.*
import kotlinx.android.synthetic.main.popup_date.view.*

class PopupDate : Popup() {
    // Today's date
    private lateinit var today: TaskDate
    // Most recently chosen date
    private var date: TaskDate = TaskDate()
    // Highlighted cell
    private var selected: SelectedPopupDateDay = SelectedPopupDateDay()
    // Data/Information pertaining to weeks in popup created
    private val data : PopupDateData = PopupDateData()
    private val weeks = data.weeks
    private val months = data.months
    private val endWeek = Settings.maxWeeks - 1
    private val startMonth = today().month
    private val endMonth = weeks[endWeek].month
    // Trackers
    private var currWeek: Int = 0
    private var currMonth: Int = 0

    fun create(attachTo: View, modify: TextView?, context: Context, edited: TaskDate, anchor: Anchor = Anchor.Above) : PopupWindow {
        val window:PopupWindow = createAndShow(context, R.layout.popup_date, attachTo, anchor)
        val view:View = window.contentView

        // 1. Get tracked variables
        today = today()                // Today's date
        date = edited.copy()           // Most recently selected date
        currMonth = date.month         // Match month to currently chosen month
        currWeek = selected.week       // Match week to currently chosen week

        // Refresh in case 12:00 midnight
        if (date.id < today.id) { date = today() }

        // 2. Setup Arrays
        // [A]. Cell underneath, text updated
        val updatedTextViews: ArrayList<TextView> = arrayListOf(
            view.txtMon, view.txtTue, view.txtWed, view.txtThu, view.txtFri, view.txtSat, view.txtSun)

        // 3. Setup text displays
        view.txtSelectedDate.text = date.asStringShort()        // Selected date
        view.txtWeek.text = date.getWeek().asString()           // Selected week
        view.txtCurrMonth.text = date.month.monthAsString()     // Current month
        setDayLabels(updatedTextViews, weeks[currWeek].days)    // Days in week

        // 4. Check whether we need to hide forward/backward buttons for both weeks/months
        when (currWeek) {
            0 ->       view.btnWeekPrev.visibility = View.INVISIBLE   // This week, hide backwards
            endWeek -> view.btnWeekNext.visibility = View.INVISIBLE   // Final week, hide forwards
        }
        // If singular month, both toggled invisible
        if (currMonth == startMonth) view.btnMonthPrev.visibility = View.INVISIBLE
        if (currMonth == endMonth)   view.btnMonthNext.visibility = View.INVISIBLE

        // 5. Setup onClick behaviours
        // [A]. Mo - Su cells
        view.mon.setupDayClickListener(0, view.txtMon, view.txtSelectedDate)
        view.tue.setupDayClickListener(1, view.txtTue, view.txtSelectedDate)
        view.wed.setupDayClickListener(2, view.txtWed, view.txtSelectedDate)
        view.thu.setupDayClickListener(3, view.txtThu, view.txtSelectedDate)
        view.fri.setupDayClickListener(4, view.txtFri, view.txtSelectedDate)
        view.sat.setupDayClickListener(5, view.txtSat, view.txtSelectedDate)
        view.sun.setupDayClickListener(6, view.txtSun, view.txtSelectedDate)

        // [B]. Week
        view.btnWeekNext.setOnClickListener {
            // Increment week and update labels
            currWeek++
            view.txtWeek.updateWeekLabel()
            setDayLabels(updatedTextViews, weeks[currWeek].days)

            // Check result
            when (currWeek) {
                // Week: 0 -> 1 (Enable back button)
                1 -> view.btnWeekPrev.visibility = View.VISIBLE
                // Week: secondLast -> last (disable forward button)
                endWeek -> view.btnWeekNext.visibility = View.INVISIBLE
            }
            // Check if month needs to be updated
            if (weeks[currWeek].month > currMonth)
                view.txtCurrMonth.monthNext(view.btnMonthPrev, view.btnMonthNext)
        }
        view.btnWeekPrev.setOnClickListener {
            // Decrement week and update labels
            currWeek--
            view.txtWeek.updateWeekLabel()
            setDayLabels(updatedTextViews, weeks[currWeek].days)

            // Check result
            when (currWeek) {
                // Week: 1 -> 0 (Disable back button)
                0 -> view.btnWeekPrev.visibility = View.INVISIBLE
                // Week: last -> secondLast (enable forward button)
                (endWeek - 1) -> view.btnWeekNext.visibility = View.VISIBLE
            }
            // Check if month needs to be updated
            if (weeks[currWeek].month < currMonth)
                view.txtCurrMonth.monthPrev(view.btnMonthPrev, view.btnMonthNext)
        }

        // [C]. Month
        view.btnMonthNext.setOnClickListener {
            view.txtCurrMonth.monthNext(view.btnMonthPrev, view.btnMonthNext)

            // If moving from starting week 0 -> x. Enable week prev button
            if (currWeek == 0) view.btnWeekPrev.visibility = View.VISIBLE

            // Update week to match month forward jump, then update displays
            currWeek = months[currMonth]!!
            view.txtWeek.updateWeekLabel()
            setDayLabels(updatedTextViews, weeks[currWeek].days)

            // If last week reached disable week forward button
            if (currWeek == endWeek) view.btnWeekNext.visibility = View.INVISIBLE
        }
        view.btnMonthPrev.setOnClickListener {
            view.txtCurrMonth.monthPrev(view.btnMonthPrev, view.btnMonthNext)

            // If current week was endWeek, re-enable forward button
            if (currWeek == endWeek) view.btnWeekNext.visibility = View.VISIBLE

            // Update week to match month backward jump, update labels
            currWeek = months[currMonth]!!
            view.txtWeek.updateWeekLabel()
            setDayLabels(updatedTextViews, weeks[currWeek].days)

            // If week back to 0, disable week back button
            if (currWeek == 0) view.btnWeekPrev.visibility = View.INVISIBLE
        }

        // [D]. Apply changes
        view.btnApplyDate.setOnClickListener {
            // Store current week selected cell is in
            selected.week = currWeek

            edited.replace(date)
            modify?.text = date.asStringShort()
            window.dismiss()
        }

        // ToDo:
        // Reset button

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
            val clickedDay: PopupDateDay = weeks[currWeek].days[dayIndex]
            // Two cases where nothing is done:
            // [A]. Not selectable, label == "-".
            // [B]. Already selected, highlighted and displayed above
            if (clickedDay.label != "-" && (clickedDay.taskDate.id != date.id)) {
                // Update label and selected date
                date = clickedDay.taskDate
                updatedTextView.text = clickedDay.taskDate.asStringShort()

                // Clear previous highlight, switch then highlight new cell view
                selected.applyBackgroundColor(Color.WHITE)
                selected.view = highlightedView
                selected.applyBackgroundColor(Settings.highlightColor)
            }
        }
    }
    private fun setDayLabels(textViews: ArrayList<TextView>, dayData: ArrayList<PopupDateDay>) {
        for (index: Int in 0..textViews.lastIndex) {
            // Set string attached to each day label
            textViews[index].text = dayData[index].label
            // Highlight date yellow if selected
            if (dayData[index].taskDate.id == date.id) {
                selected.view = textViews[index]
                selected.applyBackgroundColor(Settings.highlightColor)
            }
        }
    }

    private fun TextView.updateWeekLabel() {
        this.text = weeks[currWeek].week.asString()      // Update text display
        selected.applyBackgroundColor(Color.WHITE)   // Unhighlight selectedCell
    }

    private fun TextView.monthNext(btnMonthPrev: ImageView, btnMonthNext: ImageView) {
        currMonth++
        this.text = currMonth.monthAsString()

        // startMonth -> startMonth + 1. Enable back button
        if (currMonth == startMonth + 1) btnMonthPrev.visibility = View.VISIBLE
        // lastMonth - 1 -> lastMonth. Disable forward button
        if (currMonth == endMonth) btnMonthNext.visibility = View.INVISIBLE
    }
    private fun TextView.monthPrev(btnMonthPrev: ImageView, btnMonthNext: ImageView) {
        currMonth--
        this.text = currMonth.monthAsString()

        // startMonth + 1 -> startMonth. Disable back button
        if (currMonth == startMonth) btnMonthPrev.visibility = View.INVISIBLE
        // lastMonth -> lastMonth - 1. Enable forward button
        if (currMonth == endMonth - 1) btnMonthNext.visibility = View.VISIBLE
    }
}
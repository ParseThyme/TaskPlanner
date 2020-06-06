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
import com.example.myapplication.utility.debugMessagePrint
import kotlinx.android.synthetic.main.popup_date.view.*

class PopupDate : Popup() {
    // Most recently chosen date
    private var chosenDate: TaskDate = TaskDate()
    // Highlighted cell
    private var chosenDateView: SelectedPopupDateDay = SelectedPopupDateDay()
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
        chosenDate = edited.copy()             // Most recently selected date
        currWeek = chosenDate.getWeekNum()     // Match week to currently chosen date

        // Refresh in case of 12:00 midnight (today's date now next day)
        if (chosenDate.isPastDate()) {
            data.refreshEntries()
            AppData.firstDayOfWeek = today().firstDayOfWeek()
            chosenDate = today()
            edited.replace(chosenDate)
            modify?.text = today().asStringShort()
            currWeek = 0
        }

        // Match month to currently chosen month, copy over currWeek
        currMonth = chosenDate.month
        chosenDateView.week = currWeek

        // 2. Setup Arrays
        // [A]. Cell underneath, text updated
        val updatedTextViews: ArrayList<TextView> = arrayListOf(
            view.txtSun, view.txtMon, view.txtTue, view.txtWed, view.txtThu, view.txtFri, view.txtSat)

        // 3. Setup text displays, based on current parameters
        view.txtChosenDate.text = chosenDate.asStringShort()         // Date
        view.txtWeek.text = chosenDate.getWeek().asString()          // Week
        view.txtCurrMonth.text = chosenDate.month.monthAsString()    // Month
        setDayLabels(updatedTextViews, weeks[currWeek].days)         // Days in week

        // 4. Check whether we need to hide forward/backward buttons for both weeks/months
        when (currWeek) {
            0 ->       view.btnWeekPrev.visibility = View.INVISIBLE   // This week, hide backwards
            endWeek -> view.btnWeekNext.visibility = View.INVISIBLE   // Final week, hide forwards
        }
        // If singular month, both prev/forward buttons set invisible
        if (currMonth == startMonth) view.btnMonthPrev.visibility = View.INVISIBLE
        if (currMonth == endMonth)   view.btnMonthNext.visibility = View.INVISIBLE

        // 5. Setup onClick behaviours
        // [A]. Mo - Su cells
        view.layoutSun.setupDayClickListener(0, view.txtSun, view.txtChosenDate)
        view.layoutMon.setupDayClickListener(1, view.txtMon, view.txtChosenDate)
        view.layoutTue.setupDayClickListener(2, view.txtTue, view.txtChosenDate)
        view.layoutWed.setupDayClickListener(3, view.txtWed, view.txtChosenDate)
        view.layoutThu.setupDayClickListener(4, view.txtThu, view.txtChosenDate)
        view.layoutFri.setupDayClickListener(5, view.txtFri, view.txtChosenDate)
        view.layoutSat.setupDayClickListener(6, view.txtSat, view.txtChosenDate)

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

        // [D]. Apply changes / Dismiss window
        view.btnApplyDate.setOnClickListener {
            // Store current week selected cell is in
            chosenDateView.week = currWeek
            // Update id if -1 (Need to assign correct id)
            if (chosenDate.id == -1) chosenDate.createID()
            // Update passed in date
            edited.replace(chosenDate)
            modify?.text = chosenDate.asStringShort()
            window.dismiss()
        }
        view.dateDismissLeft.setOnClickListener { window.dismiss() }
        view.dateDismissRight.setOnClickListener { window.dismiss() }

        // [E]. Reset button, selected date jump
        view.btnResetDate.setOnClickListener {
            // Do nothing if today is chosen, otherwise
            if (!(chosenDate.same(today()) && currWeek == 0)) {
                // Set chosenDate to today
                chosenDate = today()
                view.txtChosenDate.text = chosenDate.asStringShort()

                // If not at first week, jump to it
                when (currWeek) {
                    // Clear highlight of previously chosenDate
                    0 -> chosenDateView.applyBackgroundColor(Color.WHITE)
                    // Move to week 0
                    else -> {
                        // Set current week, month to first entries respectively
                        currWeek = 0
                        currMonth = weeks[0].month
                        chosenDateView.week = 0

                        // Update week and month label displays
                        view.txtWeek.updateWeekLabel()
                        view.txtCurrMonth.text = currMonth.monthAsString()

                        // Disable back buttons & re-enable forward
                        view.btnWeekPrev.visibility = View.INVISIBLE
                        view.btnMonthPrev.visibility = View.INVISIBLE
                        view.btnWeekNext.visibility = View.VISIBLE
                        view.btnMonthNext.visibility = View.VISIBLE
                    }
                }

                setDayLabels(updatedTextViews, weeks[currWeek].days)    // Apply highlight
            }
        }
        view.txtChosenDate.setOnClickListener {
            // Perform jump when not at currently selected week
            if (currWeek != chosenDateView.week) {
                // Move week and month to currently selected date
                currWeek = chosenDateView.week
                currMonth = weeks[currWeek].month
                // Update view labels
                view.txtWeek.updateWeekLabel()
                setDayLabels(updatedTextViews, weeks[currWeek].days)
                view.txtCurrMonth.text = currMonth.monthAsString()
                // Update back/forward buttons accordingly
                // [A]. Week
                when (currWeek) {
                    0 -> {
                        view.btnWeekPrev.visibility = View.INVISIBLE
                        view.btnWeekNext.visibility = View.VISIBLE
                    }
                    weeks.lastIndex -> {
                        view.btnWeekNext.visibility = View.INVISIBLE
                        view.btnWeekPrev.visibility = View.VISIBLE
                    }
                    else -> {
                        view.btnWeekNext.visibility = View.VISIBLE
                        view.btnWeekPrev.visibility = View.VISIBLE
                    }
                }
                // [B]. Month
                when (currMonth) {
                    startMonth -> {
                        view.btnMonthPrev.visibility = View.INVISIBLE
                        view.btnMonthNext.visibility = View.VISIBLE
                    }
                    endMonth -> {
                        view.btnMonthNext.visibility = View.INVISIBLE
                        view.btnMonthPrev.visibility = View.VISIBLE
                    }
                    else -> {
                        view.btnMonthNext.visibility = View.VISIBLE
                        view.btnMonthPrev.visibility = View.VISIBLE
                    }
                }
            }
        }

        return window
    }

    private fun View.setupDayClickListener(dayIndex: Int, highlightedView: TextView, selectedTextView: TextView) {
        setOnClickListener {
            val clickedDay: PopupDateDay = weeks[currWeek].days[dayIndex]
            // Two cases where nothing is done:
            // [A]. Not selectable, label == "-".
            // [B]. Already selected, highlighted and displayed above
            if (clickedDay.label != "-" && !clickedDay.taskDate.same(chosenDate)) {
                // Update label and selected date
                chosenDate = clickedDay.taskDate
                selectedTextView.text = clickedDay.taskDate.asStringShort()

                // Update selected params: clear previous highlight, switch then highlight new cell view
                chosenDateView.week = currWeek
                chosenDateView.applyBackgroundColor(Color.WHITE)
                chosenDateView.view = highlightedView
                chosenDateView.applyBackgroundColor(Settings.highlightColor)
            }
        }
    }
    private fun setDayLabels(textViews: ArrayList<TextView>, dayData: ArrayList<PopupDateDay>) {
        for (index: Int in 0..textViews.lastIndex) {
            // Set string attached to each day label
            textViews[index].text = dayData[index].label
            // Highlight date yellow if selected
            if (dayData[index].taskDate.same(chosenDate)) {
                chosenDateView.view = textViews[index]
                chosenDateView.applyBackgroundColor(Settings.highlightColor)
            }
        }
    }

    private fun TextView.updateWeekLabel() {
        this.text = weeks[currWeek].week.asString()      // Update text display
        chosenDateView.applyBackgroundColor(Color.WHITE)   // Unhighlight selectedCell
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
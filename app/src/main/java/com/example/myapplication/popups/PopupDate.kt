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
import com.example.myapplication.singletons.AppData
import kotlinx.android.synthetic.main.popup_date.view.*

class PopupDate : Popup() {
    // Most recently chosen date
    private var chosenDate: TaskDate = TaskDate()
    // Highlighted cell
    private var chosenDateView: SelectedPopupDateDay = SelectedPopupDateDay()
    // Data/Information pertaining to weeks in popup created
    private val data : PopupDateData = PopupDateData()
    private val endWeek = Settings.maxWeeks - 1
    // Trackers
    private var currWeek: Int = 0
    private var currMonth: Int = 0

    fun create(attachTo: View, modify: TextView?, context: Context, edited: TaskDate) : PopupWindow {
        val window:PopupWindow = createAndShow(context, R.layout.popup_date, attachTo)
        val view:View = window.contentView

        // 1. Get tracked variables
        chosenDate = edited.copy()             // Most recently selected date
        currWeek = chosenDate.getWeekNum()     // Match week to currently chosen date

        // Check whether values needs to be refreshed:
        // Entry list is outdated, refresh entries
        if (data.outdated()){
            data.refreshEntries()
            AppData.firstDayOfWeek = data.getDay(0, 0).taskDate
        }
        // Chosen Date is past date = set chosen date to today
        if (chosenDate.isPastDate()) {
            currWeek = 0
            chosenDate = today()
            edited.replace(chosenDate)
            modify?.text = today().asStringShort()
        }

        // Match month to currently chosen month, copy over currWeek
        currMonth = chosenDate.month
        chosenDateView.week = currWeek

        // 2. Setup Arrays
        // [A]. Cell underneath, text updated
        val updatedTextViews: ArrayList<TextView> = arrayListOf(
            view.txtSun, view.txtMon, view.txtTue, view.txtWed, view.txtThu, view.txtFri, view.txtSat)

        // 3. Setup text displays, based on current parameters
        view.txtChosenDate.text = chosenDate.asStringShort()       // Date
        view.txtWeek.text = chosenDate.weekAsString()              // Week
        view.txtMonth.text = chosenDate.monthAsString()            // Month
        setDayLabels(updatedTextViews, data.getWeek(currWeek))     // Days in week

        // 4. Check whether we need to hide forward/backward buttons for both weeks/months
        when (currWeek) {
            0 ->       view.btnWeekPrev.visibility = View.INVISIBLE   // This week, hide backwards
            endWeek -> view.btnWeekNext.visibility = View.INVISIBLE   // Final week, hide forwards
        }
        // If singular month, both prev/forward buttons set invisible
        if (currMonth == data.startMonth) view.btnMonthPrev.visibility = View.INVISIBLE
        if (currMonth == data.endMonth)   view.btnMonthNext.visibility = View.INVISIBLE

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
            setDayLabels(updatedTextViews, data.getWeek(currWeek))

            // Check result
            when (currWeek) {
                // Week: 0 -> 1 (Enable back button)
                1 -> view.btnWeekPrev.visibility = View.VISIBLE
                // Week: secondLast -> last (disable forward button)
                endWeek -> view.btnWeekNext.visibility = View.INVISIBLE
            }

            // Check if month needs to be updated
            if (data.weeks[currWeek].month > currMonth)
                view.txtMonth.monthNext(view.btnMonthPrev, view.btnMonthNext)
        }
        view.btnWeekPrev.setOnClickListener {
            // Decrement week and update labels
            currWeek--
            view.txtWeek.updateWeekLabel()
            setDayLabels(updatedTextViews, data.getWeek(currWeek))

            // Check result
            when (currWeek) {
                // Week: 1 -> 0 (Disable back button)
                0 -> view.btnWeekPrev.visibility = View.INVISIBLE
                // Week: last -> secondLast (enable forward button)
                (endWeek - 1) -> view.btnWeekNext.visibility = View.VISIBLE
            }
            // Check if month needs to be updated
            if (data.weeks[currWeek].month < currMonth)
                view.txtMonth.monthPrev(view.btnMonthPrev, view.btnMonthNext)
        }

        // [C]. Month
        view.btnMonthNext.setOnClickListener {
            view.txtMonth.monthNext(view.btnMonthPrev, view.btnMonthNext)

            // If moving from starting week 0 -> x. Enable week prev button
            if (currWeek == 0) view.btnWeekPrev.visibility = View.VISIBLE

            // Update week to match month forward jump, then update displays
            currWeek = data.months[currMonth]!!
            view.txtWeek.updateWeekLabel()
            setDayLabels(updatedTextViews, data.getWeek(currWeek))

            // If last week reached disable week forward button
            if (currWeek == endWeek) view.btnWeekNext.visibility = View.INVISIBLE
        }
        view.btnMonthPrev.setOnClickListener {
            view.txtMonth.monthPrev(view.btnMonthPrev, view.btnMonthNext)

            // If current week was endWeek, re-enable forward button
            if (currWeek == endWeek) view.btnWeekNext.visibility = View.VISIBLE

            // Update week to match month backward jump, update labels
            currWeek = data.months[currMonth]!!
            view.txtWeek.updateWeekLabel()
            setDayLabels(updatedTextViews, data.getWeek(currWeek))

            // If week back to 0, disable week back button
            if (currWeek == 0) view.btnWeekPrev.visibility = View.INVISIBLE
        }

        // [D]. Apply changes, Dismiss window, Reset Button
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
        view.btnResetDate.setOnClickListener {
            // Do nothing if today is chosen, otherwise
            if (!(chosenDate.same(today()) && currWeek == 0)) {
                // Set chosenDate to today
                chosenDate = today()
                chosenDateView.week = 0
                view.txtChosenDate.text = chosenDate.asStringShort()

                // If at week 0, simply clear highlight of previously selected date otherwise jump to 0
                when (currWeek) {
                       0 -> chosenDateView.applyBackgroundColor(Color.WHITE)    // Clear highlight
                    else -> view.toFirstWeek(updatedTextViews, data.getWeek(0))   // Move to week 0
                }
                setDayLabels(updatedTextViews, data.getWeek(currWeek))    // Apply highlight
            }
        }

        // Shortcuts
        view.txtChosenDate.setOnClickListener {
            // Perform jump when not at currently selected week
            if (currWeek != chosenDateView.week) {
                // Move week and month to currently selected date
                currWeek = chosenDateView.week
                currMonth = data.weeks[currWeek].month
                // Update view labels
                view.txtWeek.updateWeekLabel()
                setDayLabels(updatedTextViews, data.getWeek(currWeek))
                view.txtMonth.text = currMonth.monthAsString()
                // Update back/forward buttons accordingly
                // [A]. Week
                when (currWeek) {
                    0 -> {
                        view.btnWeekPrev.visibility = View.INVISIBLE
                        view.btnWeekNext.visibility = View.VISIBLE
                    }
                    data.weeks.lastIndex -> {
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
                    data.startMonth -> {
                        view.btnMonthPrev.visibility = View.INVISIBLE
                        view.btnMonthNext.visibility = View.VISIBLE
                    }
                    data.endMonth -> {
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
        view.txtWeek.setOnClickListener {
            // Jump to week 0, if at 0, then jump to last week
            when (currWeek) {
                0 -> { // To last week
                    // Set current week, month to first respectively
                    currWeek = endWeek
                    currMonth = data.getMonth(endWeek)

                    // Update week and month label displays
                    view.txtWeek.updateWeekLabel()
                    setDayLabels(updatedTextViews, data.getWeek(currWeek))
                    view.txtMonth.text = currMonth.monthAsString()

                    // Disable forward buttons & re-enable backward
                    view.btnWeekPrev.visibility = View.VISIBLE
                    view.btnMonthPrev.visibility = View.VISIBLE
                    view.btnWeekNext.visibility = View.INVISIBLE
                    view.btnMonthNext.visibility = View.INVISIBLE
                }
                else -> view.toFirstWeek(updatedTextViews, data.getWeek(0))
            }
        }

        return window
    }

    private fun View.setupDayClickListener(dayIndex: Int, highlightedView: TextView, selectedTextView: TextView) {
        setOnClickListener {
            val clickedDay: PopupDateDay = data.getDay(dayIndex, currWeek)
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

    private fun View.toFirstWeek(textViews: ArrayList<TextView>, dayData: ArrayList<PopupDateDay>) {
        // Set current week, month to first respectively
        currWeek = 0
        currMonth = data.weeks[currWeek].month

        // Update week and month label displays
        txtWeek.updateWeekLabel()
        setDayLabels(textViews, dayData)
        txtMonth.text = currMonth.monthAsString()

        // Disable back buttons & re-enable forward
        btnWeekPrev.visibility = View.INVISIBLE
        btnMonthPrev.visibility = View.INVISIBLE
        btnWeekNext.visibility = View.VISIBLE
        btnMonthNext.visibility = View.VISIBLE
    }
    private fun TextView.updateWeekLabel() {
        this.text = data.weeks[currWeek].week.asString()   // Update text display
        chosenDateView.applyBackgroundColor(Color.WHITE)   // Unhighlight selectedCell
    }

    private fun TextView.monthNext(btnMonthPrev: ImageView, btnMonthNext: ImageView) {
        currMonth++
        this.text = currMonth.monthAsString()

        when {
            // startMonth -> startMonth + 1. Enable back button
            (currMonth == data.startMonth + 1) -> btnMonthPrev.visibility = View.VISIBLE
            // lastMonth - 1 -> lastMonth. Disable forward button
            (currMonth == data.endMonth) -> btnMonthNext.visibility = View.INVISIBLE
        }
    }
    private fun TextView.monthPrev(btnMonthPrev: ImageView, btnMonthNext: ImageView) {
        currMonth--
        this.text = currMonth.monthAsString()

        when {
            // startMonth + 1 -> startMonth. Disable back button
            (currMonth == data.startMonth) -> btnMonthPrev.visibility = View.INVISIBLE
            // lastMonth -> lastMonth - 1. Enable forward button
            (currMonth == data.endMonth - 1) -> btnMonthNext.visibility = View.VISIBLE
        }
    }
}
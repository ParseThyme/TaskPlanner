package com.example.myapplication.popups

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.*
import com.example.myapplication.data_classes.*
import com.example.myapplication.singletons.Settings
import kotlinx.android.synthetic.main.popup_date.view.*
import kotlinx.android.synthetic.main.popup_date_day.view.*

class PopupDate : Popup() {
    // Most recently chosen date, highlighted cell
    private var chosenDate: TaskDate = TaskDate()
    // Data/Information pertaining to weeks in popup created
    private var startMonth = today().month
    private var endMonth = startMonth + Settings.maxMonths

    // Trackers
    private var currMonth: Int = 0
    private var currYear: Int = 0
    private var days: Int = currMonth.monthLength(today().year)
    private lateinit var dateAdapter: PopupDateAdapter
    var update: Boolean = false

    fun create(edited: TaskDate, context: Context, attachTo: View, modify: TextView? = null) : PopupWindow {
        val window:PopupWindow = create(context, R.layout.popup_date)
        val view:View = window.contentView

        update = false // Apply changes if apply button pressed. Otherwise counts as exit

        createMonthList()

        // 1. Get tracked variables
        chosenDate = edited.copy()             // Most recently selected date
        currMonth = chosenDate.month
        currYear = today().year

        // If data outdated, refresh
        refreshData()

        // Set displays
        // Text
        view.txtChosenDate.text = chosenDate.asStringShort()
        view.txtMonth.text = chosenDate.monthLabel()
        view.rvPopupDateDays.apply {
            dateAdapter = PopupDateAdapter(days, view.txtChosenDate)
            layoutManager = GridLayoutManager(context, 7)
            adapter = dateAdapter
        }

        // Month
        // Enable/Disable back/forward buttons based on month
        when (currMonth) {
            startMonth -> view.btnMonthPrev.visibility = View.INVISIBLE
              endMonth -> view.btnMonthNext.visibility = View.INVISIBLE
        }
        view.btnMonthNext.setOnClickListener { view.monthNext() }
        view.btnMonthPrev.setOnClickListener { view.txtMonth.monthPrev(view.btnMonthPrev, view.btnMonthNext) }

        /*
        // 1. Get tracked variables
        chosenDate = edited.copy()             // Most recently selected date
        currWeek = chosenDate.getWeekNum()     // Match week to currently chosen date

        // 2. Check whether values needs to be refreshed:
        // [A]. Entry list is outdated, refresh entries
        data.checkOutdated()
        // [B]. Chosen Date is past date = set chosen date to today
        if (chosenDate.isPastDate()) {
            currWeek = 0
            chosenDate = today()
            edited.replace(chosenDate)
            modify?.text = today().asStringShort()
        }

        // Match month to currently chosen month, copy over currWeek
        currMonth = chosenDate.month
        chosenDayInWeekView.week = currWeek

        view.btnResetDate.setOnClickListener {
            // Do nothing if today is chosen, otherwise
            if (!(chosenDate.same(today()) && currWeek == 0)) {
                // Set chosenDate to today
                chosenDate = today()
                chosenDayInWeekView.week = 0
                view.txtChosenDate.text = chosenDate.asStringShort()

                // If at week 0, simply clear highlight of previously selected date otherwise jump to 0
                when (currWeek) {
                       0 -> chosenDayInWeekView.applyBackgroundColor(Color.WHITE)    // Clear highlight
                    else -> view.toFirstWeek(updatedTextViews, data.getWeek(0))   // Move to week 0
                }
                setDayLabels(updatedTextViews, data.getWeek(currWeek))    // Apply highlight
            }
        }

        // [E]. Shortcuts
        view.txtChosenDate.setOnClickListener {
            // Perform jump when not at currently selected week
            if (currWeek != chosenDayInWeekView.week) {
                // Move week and month to currently selected date
                currWeek = chosenDayInWeekView.week
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
        */

        // Apply changes, Dismiss window, Reset Button
        view.btnApplyDate.setOnClickListener {
            // Update passed in date
            edited.replace(chosenDate)
            modify?.text = chosenDate.asStringShort()
            update = true
            window.dismiss()
        }

        view.dateDismissLeft.setOnClickListener { window.dismiss() }
        view.dateDismissRight.setOnClickListener { window.dismiss() }

        window.show(attachTo)
        return window
    }

    private fun refreshData() {
        startMonth = today().month
        endMonth = startMonth + Settings.maxMonths
    }

    private fun View.monthNext() {
        currMonth++
        dateAdapter.updateMonth()
        txtMonth.text = currMonth.monthLabel()
        when {
            // startMonth -> startMonth + 1. Enable back button
            (currMonth == startMonth + 1) -> btnMonthPrev.visibility = View.VISIBLE
            // lastMonth - 1 -> lastMonth. Disable forward button
            (currMonth == endMonth) -> btnMonthNext.visibility = View.INVISIBLE
        }
    }
    private fun TextView.monthPrev(btnMonthPrev: ImageView, btnMonthNext: ImageView) {
        currMonth--
        dateAdapter.updateMonth()
        text = currMonth.monthLabel()

        when {
            // startMonth + 1 -> startMonth. Disable back button
            (currMonth == startMonth) -> btnMonthPrev.visibility = View.INVISIBLE
            // lastMonth -> lastMonth - 1. Enable forward button
            (currMonth == endMonth - 1) -> btnMonthNext.visibility = View.VISIBLE
        }
    }

    // class PopupDateAdapter (private val taskList: ArrayList<PopupDateDay>, private val closeFn: (String) -> Unit)
    inner class PopupDateAdapter (private var dayCount: Int, private val txtChosenDate: TextView)
        : RecyclerView.Adapter<PopupDateAdapter.ViewHolder>() {
        private var days: ArrayList<Int> = arrayListOf(
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
            24, 25, 26, 27, 28, 29, 30, 31)

        override fun getItemCount(): Int { return dayCount }
        override fun onBindViewHolder(holder: ViewHolder, pos: Int) { holder.bind(days[pos]) }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent.inflate(R.layout.popup_date_day))
        }

        var selected: View? = null
        var today: Int = today().day

        // ToDo: Consider variable: clickable [T/F]

        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            fun bind(day: Int) {
                // Highlight selected day
                if (day == chosenDate.day && currMonth == chosenDate.month) updateHighlight()

                // Bind text
                when {
                    // 1. Show "-" for past dates (Only when currMonth is starting month)
                    (day < today && currMonth == startMonth) -> itemView.txtDay.text = "-"

                    // 2. Completely hide unreachable days. E.g. day 30/31 for February (28/29 = max)
                    (day > dayCount) -> itemView.txtDay.text = ""

                    // 3. Standard day, clickable and selectable
                    else -> {
                        itemView.txtDay.text = day.toString()
                        itemView.txtDay.setOnClickListener {
                            // Do nothing if already selected or is past date
                            if (selectable(day)) {
                                // Update chosen date and textView display
                                chosenDate.day = day
                                chosenDate.month = currMonth
                                // Year + 1 if month < starting month. E.g. start at Nov and selected Jan
                                chosenDate.year = when (currMonth < startMonth) {
                                    true -> today().year + 1
                                    false -> today().year
                                }
                                chosenDate.createID()
                                txtChosenDate.text = chosenDate.asStringShort()

                                // Update highlight
                                updateHighlight()
                            }
                        }
                    }
                }
            }

            private fun updateHighlight() {
                // Clear highlight from previously selected, update highlight new selection
                selected?.applyBackgroundColor()
                selected = itemView
                selected!!.applyBackgroundColor(Settings.highlightColor)
            }

            private fun selectable(day: Int) : Boolean {
                // Selectable when: Not past date and not already selected
                return when {
                    // 1. Starting month, check if past day clicked
                    (currMonth == startMonth && day < today) -> false
                    // 2. Already selected
                    (chosenDate.month == currMonth && chosenDate.day == day) -> false
                    else -> true
                }
            }
        }

        fun updateMonth() {
            val oldCount: Int = dayCount                  // Store copy of old day count
            dayCount = currMonth.monthLength(currYear)    // Get new day count

            // 1. Toggle days set as "-". Moving to/from start month. Days from day 1 to today - 1 need to be shown/hidden
            if (currMonth == startMonth + 1 || currMonth == startMonth) {
                notifyItemRangeChanged(0, today - 1)
            }

            // 2. Toggle highlighting of chosen date. Hide if not at selected month (F) otherwise reshow (T)
            when (currMonth == chosenDate.month) {
                 true -> selected!!.applyBackgroundColor(Settings.highlightColor)
                false -> selected!!.applyBackgroundColor()
            }

            // 3. Update trailing days if dayCount has been updated (28, 29, 30, 31)
            if (oldCount == dayCount) return
            // Cases:
            when {
                // Standard Year:
                // 1. Feb -> Jan OR Feb -> Mar: 28 days -> 31 days. Update day 29, 30, 31
                // 2. Feb <- Jan OR Feb <- Mar: 28 days <- 31 days. Same as above
                (oldCount == 28 || dayCount == 28) ->
                    notifyItemRangeChanged(days.lastIndex - 3, 3)

                // Leap Year: Same two conditions as above, but with 29 days
                (oldCount == 29 || dayCount == 29) ->
                    notifyItemRangeChanged(days.lastIndex - 2, 2)

                // Odd -> Even day month or vice versa
                (dayCount == 31 || dayCount == 30) -> notifyItemChanged(days.lastIndex)
            }
        }
    }
}
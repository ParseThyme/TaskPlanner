package com.example.myapplication.popups

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
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
import java.util.*
import kotlin.collections.ArrayList

class PopupDate : Popup() {
    var update: Boolean = false

    // Most recently chosen date
    private var chosenDate: TaskDate = TaskDate()

    // Month/day list data
    private var currMonth: Int = 0
    private var startMonth = today().month
    private var endMonth = startMonth.getEndMonth()
    private var monthList: MutableMap<Int, ArrayList<TaskDate>> = createMonthList()

    private lateinit var dateAdapter: PopupDateAdapter

    fun create(edited: TaskDate, context: Context, attachTo: View, modify: TextView? = null) : PopupWindow {
        val window:PopupWindow = create(context, R.layout.popup_date)
        val view:View = window.contentView

        update = false // Apply changes if apply button pressed. Otherwise counts as exit

        // Most recently selected date
        chosenDate = edited.copy()

        // If data outdated, refresh
        refreshData(edited, modify)

        // Set month & year
        currMonth = chosenDate.month

        // Set displays
        // Text
        view.txtChosenDate.text = chosenDate.asStringShort()
        view.txtMonth.text = chosenDate.monthLabel()
        view.rvPopupDateDays.apply {
            dateAdapter = PopupDateAdapter(monthList[currMonth]!!, view.txtChosenDate)
            layoutManager = GridLayoutManager(context, 7)
            adapter = dateAdapter
        }

        // Month
        // Enable/Disable back/forward buttons based on month
        when (currMonth) {
            startMonth -> view.btnMonthPrev.visibility = View.INVISIBLE
              endMonth -> view.btnMonthNext.visibility = View.INVISIBLE
        }
        view.btnMonthNext.setOnClickListener {
            // If at Dec, wrap around to Jan
            when (currMonth == Calendar.DECEMBER) {
                 true -> currMonth = Calendar.JANUARY
                false -> currMonth++
            }
            view.toMonth(currMonth)
        }
        view.btnMonthPrev.setOnClickListener {
            // If at Jan, wrap around to Dec
            when (currMonth == Calendar.JANUARY) {
                true -> currMonth = Calendar.DECEMBER
                false -> currMonth--
            }
            view.toMonth(currMonth)
        }

        // Apply changes, Shortcuts
        view.btnApplyDate.setOnClickListener {
            // Update passed in date
            edited.replace(chosenDate)
            modify?.text = chosenDate.asStringShort()
            update = true
            window.dismiss()
        }
        view.txtMonth.setOnClickListener {
            // Jump between start/end month
            when (currMonth) {
                // If at start, jump to end
                startMonth -> view.toMonth(endMonth)
                // Otherwise jump to start
                else -> view.toMonth(startMonth)
            }
        }
        view.txtChosenDate.setOnClickListener {
            // Jump to month of currently selected date
            if (currMonth != chosenDate.month) view.toMonth(chosenDate.month)
        }

        // Dismiss Window
        view.dateDismissLeft.setOnClickListener { window.dismiss() }
        view.dateDismissRight.setOnClickListener { window.dismiss() }

        window.show(attachTo)
        return window
    }

    private fun refreshData(edited: TaskDate, modify: TextView?) {
        // Past date in month, set date to today
        if (chosenDate.isPastDate()) {
            chosenDate = today().copy()
            edited.replace(chosenDate)
            modify?.text = chosenDate.asStringShort()
        }
        // Past month, refresh list
        if (chosenDate.month < today().month) {
            startMonth = today().month
            endMonth = startMonth.getEndMonth()
            monthList.clear()
            monthList = createMonthList()
        }
    }

    private fun View.toMonth(month: Int) {
        currMonth = month
        dateAdapter.updateMonth()
        txtMonth.text = currMonth.monthLabel()
        when (currMonth) {
            // End caps, disable back/forward, enable opposite
            startMonth -> {
                btnMonthPrev.visibility = View.INVISIBLE
                btnMonthNext.visibility = View.VISIBLE
            }
            endMonth -> {
                btnMonthNext.visibility = View.INVISIBLE
                btnMonthPrev.visibility = View.VISIBLE
            }
            // Enable both forward/backwards buttons
            else -> {
                btnMonthPrev.visibility = View.VISIBLE
                btnMonthNext.visibility = View.VISIBLE
            }
        }
    }
    private fun createMonthList(): MutableMap<Int, java.util.ArrayList<TaskDate>> {
        // Using today as starting point. Move to first day of month
        var currDate: TaskDate = today()
        val monthList: MutableMap<Int, java.util.ArrayList<TaskDate>> = mutableMapOf()

        // Create x amount of months based on defined count
        for (monthIndex: Int in 0..Settings.maxMonths) {
            // Generate days for month, then move to next month
            monthList[currDate.month] = createMonthDays(currDate.month, currDate.year)
            currDate = currDate.addMonths(1)
        }

        return monthList
    }
    private fun createMonthDays(month: Int, year: Int): java.util.ArrayList<TaskDate> {
        var currDate: TaskDate = taskDate(1, month, year)       // Start on first day of month
        val dayList: java.util.ArrayList<TaskDate> = arrayListOf()             // List storing days in month

        // If month doesn't start on Mo, move backwards to it.
        // E.g. 1 = We. Need to move back 2 days to be at Mo
        currDate = currDate.firstDayOfWeek()

        for (dayIndex: Int in 0 until Settings.monthSize) {
            dayList.add(currDate.copy())
            currDate += 1
        }

        return dayList
    }

    inner class PopupDateAdapter (private var days: ArrayList<TaskDate>, private var txtChosenDate: TextView)
        : RecyclerView.Adapter<PopupDateAdapter.ViewHolder>() {

        override fun getItemCount(): Int { return days.size }
        override fun onBindViewHolder(holder: ViewHolder, pos: Int) { holder.bind(days[pos], txtChosenDate) }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent.inflate(R.layout.popup_date_day))
        }

        var selected: View? = null

        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            fun bind(taskDate: TaskDate, txtChosenDate: TextView) {
                // Determine text to display
                when {
                    // 1. Date doesn't belong to current month
                    (taskDate.isPastDate() || taskDate.month != currMonth) -> {
                        itemView.txtDay.text = "-"
                        itemView.applyBackgroundColor()     // Clear highlight color is highlighted
                    }
                    // 2. Standard Date
                    else -> {
                        itemView.txtDay.apply {
                            // Show day label
                            text = taskDate.day.toString()
                            // Highlight date if is currently chosen task
                            if (chosenDate == taskDate) updateHighlight()

                            setOnClickListener {
                                // Update chosen date if not already selected
                                if (chosenDate != taskDate) {
                                    chosenDate = taskDate.copy()
                                    txtChosenDate.text = chosenDate.asStringShort()
                                    updateHighlight()
                                }
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
        }

        fun updateMonth() {
            days = monthList[currMonth]!!
            notifyDataSetChanged()
        }
    }
}
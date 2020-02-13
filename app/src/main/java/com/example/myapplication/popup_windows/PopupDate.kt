package com.example.myapplication.popup_windows

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.PopupWindow
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.Settings
import com.example.myapplication.adapters.TaskDatesAdapter
import com.example.myapplication.applyBackgroundColor
import com.example.myapplication.data_classes.DayOfWeek
import com.example.myapplication.data_classes.TaskDate
import com.example.myapplication.data_classes.addDays
import com.example.myapplication.data_classes.getDate
import kotlinx.android.synthetic.main.date_popup_window.view.*

class PopupDate(private val parent: Button,
                private val settings: Settings,
                private val context: Context)
    : PopupWindowParent()
{
    // Index of selected date, -1 means today's date otherwise any other subsequent date in the array
    private var selected: Int = -1

    // List of dates
    private val today: TaskDate = getDate()
    private val dates: ArrayList<TaskDate> = ArrayList()

    // Parameters of selected date
    var selectedDate: TaskDate = today
    private set

    init {
        // Generate subsequent dates from today to add to list for recyclerView
        for (i in 1..28) dates.add(getDate(i))
    }

    fun create(): PopupWindow {
        val window:PopupWindow = super.create(context, R.layout.date_popup_window)
        val view: View = window.contentView

        // Get today's date
        val today:TaskDate = getDate()
        view.txtToday.text = today.labelShortest

        // 7 entries per column (matching 7 days of the week)
        val colSize = 7

        // Assign labels of subsequent days based on today
        val labelToday: DayOfWeek = today.dayName
        view.labelToday.text = "$labelToday"
        view.labelOneDay.text = "${labelToday.addDays()}"
        view.labelTwoDays.text = "${labelToday.addDays(2)}"
        view.labelThreeDays.text = "${labelToday.addDays(3)}"
        view.labelFourDays.text = "${labelToday.addDays(4)}"
        view.labelFiveDays.text = "${labelToday.addDays(5)}"
        view.labelSixDays.text = "${labelToday.addDays(6)}"
        view.labelSevenDays.text = "${labelToday.addDays(7)}"

        // Set today's date to be selected by default
        view.cardToday.applyBackgroundColor(settings.highlightColor)

        // Assign adapter to RecyclerView
        val taskDatesAdapter = TaskDatesAdapter(dates, settings, view.cardToday)
        view.datesRV.apply {
            layoutManager = GridLayoutManager(this.context, colSize, GridLayoutManager.HORIZONTAL, false)
            adapter = taskDatesAdapter
        }

        // Clicking on today card
        view.cardToday.setOnClickListener {
            taskDatesAdapter.clearCurrentlySelected()
            view.cardToday.applyBackgroundColor(settings.highlightColor)
        }

        // Closing window via clicking close
        view.btnApplyDate.setOnClickListener {
            selected = taskDatesAdapter.selected

            // Selected today's date, otherwise selected subsequent date
            selectedDate = if (selected == -1)
                getDate()
            else
                dates[selected]

            // Apply text to parent and close window
            parent.text = selectedDate.labelShort
            window.dismiss()
        }

        // Show window after recyclerView finished setting up
        window.show(parent)
        return window
    }
}
package com.example.myapplication.popups

import android.content.Context
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.data_classes.*
import com.example.myapplication.utility.Settings
import com.example.myapplication.utility.defaultTimeMsg
import kotlinx.android.synthetic.main.popup_time.view.*

class PopupTime : Popup() {
    private var chosenTime: TaskTime = TaskTime(12, 0, TimeOfDay.PM, 0)

    fun create(attachTo: View, modify: TextView?, context: Context, edited: TaskTime, anchor: Anchor = Anchor.Above) : PopupWindow {
        val window:PopupWindow = create(context, R.layout.popup_time)
        val view:View = window.contentView

        // Copy over most recent time. If time unset, set default values
        chosenTime = edited.copy()
        if (!chosenTime.isValid()) chosenTime.setDefault()

        // Check duration, prevent decrement/increment if at end caps
        when (chosenTime.duration ) {
            0 -> view.btnLengthDown.visibility = View.INVISIBLE                    // Prevent decrement
            Settings.durationMax -> view.btnLengthUp.visibility = View.INVISIBLE   // Prevent increment
        }

        // Set text display based on current time values
        view.updateDisplay()

        // Click behaviour
        // Time - Hour
        view.btnTimeHourUp.setOnClickListener { view.txtTime.updateHour(view.txtTimeOverall) }
        view.btnTimeHourDown.setOnClickListener { view.txtTime.updateHour(view.txtTimeOverall, false) }
        // Time - Min
        view.btnTimeMinUp.setOnClickListener { view.txtTime.updateMin(view.txtTimeOverall) }
        view.btnTimeMinDown.setOnClickListener { view.txtTime.updateMin(view.txtTimeOverall, false) }
        // Time - TimeOfDay
        view.txtTime.setOnClickListener {
            // Update time of day display
            chosenTime.timeOfDay = chosenTime.getOppositeTimeOfDay()
            view.txtTime.text = chosenTime.startTimeLabel()
            view.txtTimeOverall.updateTimeOverallLabel()
        }

        // Duration
        view.btnLengthUp.setOnClickListener {
            // 1. Re-enable decrements if at 0
            if (chosenTime.duration == 0) view.btnLengthDown.visibility = View.VISIBLE
            // Increment duration
            view.txtLength.updateDuration(view.txtTimeOverall)
            // 2. Disable further increments if at max duration
            if (chosenTime.duration == Settings.durationMax) view.btnLengthUp.visibility = View.INVISIBLE
        }
        view.btnLengthDown.setOnClickListener {
            // 1. Re-enable increments if at max
            if (chosenTime.duration == Settings.durationMax) view.btnLengthUp.visibility = View.VISIBLE
            // Decrement duration
            view.txtLength.updateDuration(view.txtTimeOverall,false)
            // 2. Disable further decrements if at 0
            if (chosenTime.duration == 0) view.btnLengthDown.visibility = View.INVISIBLE
        }

        // Delta value
        view.btnDeltaUp.setOnClickListener { view.txtDelta.updateDelta() }
        view.btnDeltaDown.setOnClickListener { view.txtDelta.updateDelta(false) }

        // Icon pressing
        view.iconTime.setOnClickListener {
            // Toggle between [12, 6]. If at 12:00, set to 6:00am, otherwise set to 12:00am
            when (chosenTime.hour) {
                // 1. hour == 12, set hour to 6pm
                12 -> {
                    chosenTime.hour = 6
                    chosenTime.timeOfDay = TimeOfDay.PM
                }
                // 2. hour != 12, set hour to 12am
                else -> {
                    chosenTime.hour = 12
                    chosenTime.timeOfDay = TimeOfDay.AM
                }
            }
            // Set min to 0, then update labels
            chosenTime.min = 0
            view.txtTime.text = chosenTime.startTimeLabel()
            view.txtTimeOverall.updateTimeOverallLabel()
        }
        view.iconLength.setOnClickListener {
            // Toggle between [00:00, 1:00]. If not 0, set to 0. Otherwise set to 1 hour
            when (chosenTime.duration) {
                0 -> {
                    chosenTime.duration = 60    // 1 hour
                }
                // If duration was at max, re-enable increments
                Settings.durationMax -> {
                    chosenTime.duration = 0
                    view.btnLengthUp.visibility = View.VISIBLE
                }
                else -> chosenTime.duration = 0
            }

            // When duration == 0, disable decrements
            if (chosenTime.duration == 0) view.btnLengthDown.visibility = View.INVISIBLE

            // Update text displays
            view.txtLength.text = chosenTime.durationAsString()
            view.txtTimeOverall.updateTimeOverallLabel()
        }
        view.iconDelta.setOnClickListener {
            // Toggle between [5, 30]. Set to 5. If at 5, set to 30
            Settings.timeDelta = when (Settings.timeDelta) {
                5 -> 30
                else -> 5
            }
            view.txtDelta.text = Settings.timeDeltaAsString()
        }

        // Reset all, Clear Time & Confirm
        view.btnResetTimeAll.setOnClickListener {
            chosenTime.setDefault()
            Settings.timeDelta = 5
            view.updateDisplay()
            view.btnLengthDown.visibility = View.INVISIBLE
        }
        view.btnClearTime.setOnClickListener {
            // edited.clear()
            edited.unset()
            modify?.text = defaultTimeMsg
            window.dismiss()
        }
        view.btnApplyTime.setOnClickListener {
            edited.apply {
                hour = chosenTime.hour
                min = chosenTime.min
                duration = chosenTime.duration
                timeOfDay = chosenTime.timeOfDay
            }
            modify?.text = chosenTime.overallTimeLabel()
            window.dismiss()
        }

        view.timeDismissLeft.setOnClickListener { window.dismiss() }
        view.timeDismissRight.setOnClickListener { window.dismiss() }

        window.show(attachTo)
        return window
    }

    private fun TextView.updateTimeOverallLabel() {
        text = when (chosenTime.duration > 0) {
            true  -> chosenTime.overallTimeLabel()
            false -> "Set time & Length"
        }
    }

    private fun TextView.updateHour(timeOverallView: TextView, increment: Boolean = true) {
        // Update hour then overall time display
        chosenTime.updateHour(increment)
        text = chosenTime.startTimeLabel()
        timeOverallView.updateTimeOverallLabel()
    }
    private fun TextView.updateMin(timeOverallView: TextView, increment: Boolean = true) {
        // Update min then overall time display
        chosenTime.updateMin(increment)
        text = chosenTime.startTimeLabel()
        timeOverallView.updateTimeOverallLabel()
    }
    private fun TextView.updateDuration(timeOverallView: TextView, increment: Boolean = true) {
        chosenTime.updateDuration(increment)
        text = chosenTime.durationAsString()
        timeOverallView.updateTimeOverallLabel()
    }
    private fun TextView.updateDelta(increment: Boolean = true) {
        Settings.updateTimeDelta(increment)
        text = Settings.timeDeltaAsString()
    }

    private fun View.updateDisplay() {
        // Time
        txtTime.text = chosenTime.startTimeLabel()
        txtTimeOverall.updateTimeOverallLabel()
        // Duration
        txtLength.text = chosenTime.durationAsString()
        // TimeDelta
        txtDelta.text = Settings.timeDeltaAsString()
    }
}
package com.example.myapplication.popups

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.data_classes.*
import com.example.myapplication.utility.*
import kotlinx.android.synthetic.main.popup_time.view.*

class PopupTime : Popup() {
    private var chosenTime: TaskTime = TaskTime(12, 0, TimeOfDay.PM, 0)

    fun create(attachTo: View, modify: TextView?, context: Context, edited: TaskTime) : PopupWindow {
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
        view.btnTimeHourUp.setOnClickListener { view.txtChosenTime.updateHour(view.txtChosenTime, view.btnTimeOfDay) }
        view.btnTimeHourDown.setOnClickListener { view.txtChosenTime.updateHour(view.txtChosenTime, view.btnTimeOfDay, false) }
        // Time - Min
        view.btnTimeMinUp.setOnClickListener { view.txtChosenTime.updateMin(view.txtChosenTime) }
        view.btnTimeMinDown.setOnClickListener { view.txtChosenTime.updateMin(view.txtChosenTime, false) }
        // Time - TimeOfDay
        view.btnTimeOfDay.setOnClickListener {
            // Update time of day display
            chosenTime.timeOfDay = chosenTime.getOppositeTimeOfDay()
            view.btnTimeOfDay.updateTimeOfDayDisplay()
            view.txtChosenTime.text = chosenTime.startTimeLabel()
            view.txtChosenTime.text = chosenTime.overallTimeLabel()
        }

        // Duration
        view.btnLengthUp.setOnClickListener {
            // 1. Re-enable decrements if at 0, change timer icon back
            if (chosenTime.duration == 0) {
                view.btnLengthDown.visibility = View.VISIBLE
                view.btnLength.updateDrawableLeft(R.drawable.ic_timer)
            }
            // Increment duration
            view.btnLength.updateDuration(view.txtChosenTime)
            // 2. Disable further increments if at max duration
            if (chosenTime.duration >= Settings.durationMax) view.btnLengthUp.visibility = View.INVISIBLE
        }
        view.btnLengthDown.setOnClickListener {
            // 1. Re-enable increments if at max
            if (chosenTime.duration == Settings.durationMax) view.btnLengthUp.visibility = View.VISIBLE
            // Decrement duration
            view.btnLength.updateDuration(view.txtChosenTime,false)
            // 2. Disable further decrements if at 0, change timer icon to off
            if (chosenTime.duration == 0) {
                view.btnLengthDown.visibility = View.INVISIBLE
                view.btnLength.updateDrawableLeft(R.drawable.ic_timer_off)
            }
        }

        // Delta value
        view.btnDeltaUp.setOnClickListener { view.btnDelta.updateDelta() }
        view.btnDeltaDown.setOnClickListener { view.btnDelta.updateDelta(false) }

        // Shortcuts
        view.btnHour.setOnClickListener {
            // Toggle between [12, 6]. If at 12:00, set to 6:00, otherwise set to 12:00
            when (chosenTime.hour) {
                  12 -> chosenTime.hour = 6     // 1. hour == 12, set to 6
                else -> chosenTime.hour = 12    // 2. hour != 12, set to 12
            }
            // Update labels
            view.txtChosenTime.text = chosenTime.startTimeLabel()
            view.txtChosenTime.text = chosenTime.overallTimeLabel()
        }
        view.btnMin.setOnClickListener {
            // Toggle between [00, 30]. If at 00, set to 30, otherwise set to 00
            when (chosenTime.min) {
                   0 -> chosenTime.min = 30     // 1. min == 00, set to 30
                else -> chosenTime.min = 0      // 2. min != 0, set to 0
            }
            view.txtChosenTime.text = chosenTime.startTimeLabel()
            view.txtChosenTime.text = chosenTime.overallTimeLabel()
        }
        view.btnLength.setOnClickListener {
            // Toggle between [00:00, 1:00]. If not 0, set to 0. Otherwise set to 1 hour
            when (chosenTime.duration) {
                0 -> {
                    chosenTime.duration = 60    // 1 hour
                    view.btnLengthDown.visibility = View.VISIBLE
                    view.btnLength.updateDrawableLeft(R.drawable.ic_timer)
                }
                // If duration was at max, re-enable increments
                Settings.durationMax -> {
                    chosenTime.duration = 0
                    view.btnLengthUp.visibility = View.VISIBLE
                }
                else -> chosenTime.duration = 0
            }

            // When duration == 0, disable decrements
            if (chosenTime.duration == 0) {
                view.btnLengthDown.visibility = View.INVISIBLE
                view.btnLength.updateDrawableLeft(R.drawable.ic_timer_off)
            }

            // Update text displays
            view.btnLength.text = chosenTime.durationAsString()
            view.txtChosenTime.text = chosenTime.overallTimeLabel()
        }
        view.btnDelta.setOnClickListener {
            // Toggle between [5, 30]. Set to 5. If at 5, set to 30
            Settings.timeDelta = when (Settings.timeDelta) {
                5 -> 30
                else -> 5
            }
            view.btnDelta.text = Settings.timeDeltaAsString()
        }

        // Reset all, Clear Time & Confirm
        view.btnResetTime.setOnClickListener {
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

    private fun TextView.updateHour(txtChosenTime: TextView, btnTimeOfDay: ImageView, increment: Boolean = true) {
        // Update hour then overall time display
        chosenTime.updateHour(increment)
        text = chosenTime.startTimeLabel()
        txtChosenTime.text = chosenTime.overallTimeLabel()
        btnTimeOfDay.updateTimeOfDayDisplay()
    }
    private fun TextView.updateMin(txtChosenTime: TextView, increment: Boolean = true) {
        // Update min then overall time display
        chosenTime.updateMin(increment)
        text = chosenTime.startTimeLabel()
        txtChosenTime.text = chosenTime.overallTimeLabel()
    }
    private fun TextView.updateDuration(txtChosenTime: TextView, increment: Boolean = true) {
        chosenTime.updateDuration(increment)
        text = chosenTime.durationAsString()
        txtChosenTime.text = chosenTime.overallTimeLabel()
    }
    private fun TextView.updateDelta(increment: Boolean = true) {
        Settings.updateTimeDelta(increment)
        text = Settings.timeDeltaAsString()
        SaveData.saveTimeDelta(this.context)
    }

    private fun View.updateDisplay() {
        // Time
        txtChosenTime.text = chosenTime.startTimeLabel()
        txtChosenTime.text = chosenTime.overallTimeLabel()
        // TimeOfDay
        btnTimeOfDay.updateTimeOfDayDisplay()
        // Duration
        btnLength.text = chosenTime.durationAsString()
        when (chosenTime.duration) {
            0 -> btnLength.updateDrawableLeft(R.drawable.ic_timer_off)
            else -> btnLength.updateDrawableLeft(R.drawable.ic_timer)
        }
        // TimeDelta
        btnDelta.text = Settings.timeDeltaAsString()
    }
    private fun ImageView.updateTimeOfDayDisplay() {
        when (chosenTime.timeOfDay) {
            TimeOfDay.AM -> setImageResource(R.drawable.ic_time_day)
            TimeOfDay.PM -> setImageResource(R.drawable.ic_time_night)
        }
    }
}
package com.example.myapplication.popups

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.ToggleButton
import com.example.myapplication.R
import com.example.myapplication.data_classes.*
import com.example.myapplication.defaultTimeMsg
import com.example.myapplication.singletons.SaveData
import com.example.myapplication.singletons.Settings
import kotlinx.android.synthetic.main.popup_time.view.*

class PopupTime : Popup() {
    private var chosenTime: TaskTime = TaskTime(12, 0, TimeOfDay.PM, 0)
    var update: Boolean = false

    fun create(edited: TaskTime, attachTo: View, modify: TextView? = null) : PopupWindow {
        val window:PopupWindow = create(attachTo.context, R.layout.popup_time)
        val view:View = window.contentView

        update = false // Apply changes if apply button pressed or remove time. Otherwise counts as exit

        // Copy over most recent time. If time unset, set default values
        chosenTime = edited.copy()
        if (chosenTime.isUnset()) chosenTime.setDefault()

        // Check duration, prevent decrement/increment if at end caps
        when (chosenTime.duration ) {
            0 -> view.btnLengthDown.visibility = View.INVISIBLE                    // Prevent decrement
            Settings.durationMax -> view.btnLengthUp.visibility = View.INVISIBLE   // Prevent increment
        }

        // Set text display based on current time values
        // Time, TimeOfDay, Duration & TimeDelta
        view.txtChosenTime.text = chosenTime.overallTimeLabel()
        view.toggleTimeOfDay.updateTimeOfDayDisplay()
        view.toggleLength.updateDurationDisplay()
        view.btnDelta.text = Settings.timeDeltaAsString()

        // Click behaviour
        // Time - Hour
        view.btnTimeHourUp.setOnClickListener { view.txtChosenTime.updateHour(view.toggleTimeOfDay) }
        view.btnTimeHourDown.setOnClickListener { view.txtChosenTime.updateHour(view.toggleTimeOfDay,false) }
        // Time - Min
        view.btnTimeMinUp.setOnClickListener { view.txtChosenTime.updateMin() }
        view.btnTimeMinDown.setOnClickListener { view.txtChosenTime.updateMin(false) }
        // Time - TimeOfDay
        view.toggleTimeOfDay.setOnClickListener {
            // Update time of day display
            // chosenTime.timeOfDay = chosenTime.getOppositeTimeOfDay()
            when (view.toggleTimeOfDay.isChecked) {
                 true -> chosenTime.timeOfDay = TimeOfDay.AM    // Day
                false -> chosenTime.timeOfDay = TimeOfDay.PM    // Night
            }
            view.txtChosenTime.text = chosenTime.overallTimeLabel()
        }

        // Duration
        view.btnLengthUp.setOnClickListener {
            // 1. Re-enable decrements if at 0, change timer icon back
            if (!view.toggleLength.isChecked) {
                view.btnLengthDown.visibility = View.VISIBLE
                view.toggleLength.isChecked = true
            }
            // Increment duration
            view.txtChosenTime.updateLength(view.toggleLength)
            // 2. Disable further increments if at max duration
            if (chosenTime.duration >= Settings.durationMax) view.btnLengthUp.visibility = View.INVISIBLE
        }
        view.btnLengthDown.setOnClickListener {
            // 1. Re-enable increments if at max
            if (chosenTime.duration == Settings.durationMax) view.btnLengthUp.visibility = View.VISIBLE
            // Decrement duration
            view.txtChosenTime.updateLength(view.toggleLength, false)
            // 2. Disable further decrements if at 0, change timer icon to off
            if (chosenTime.duration == 0) {
                view.toggleLength.isChecked = false
                view.btnLengthDown.visibility = View.INVISIBLE
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
            // Update label
            view.txtChosenTime.text = chosenTime.overallTimeLabel()
        }
        view.btnMin.setOnClickListener {
            // Toggle between [00, 30]. If at 00, set to 30, otherwise set to 00
            when (chosenTime.min) {
                   0 -> chosenTime.min = 30     // 1. min == 00, set to 30
                else -> chosenTime.min = 0      // 2. min != 0, set to 0
            }
            view.txtChosenTime.text = chosenTime.overallTimeLabel()
        }
        view.toggleLength.setOnClickListener {
            // Toggle between [00:00, 1:00]. If not 0, set to 0. Otherwise set to 1 hour
            when (view.toggleLength.isChecked) {
                // Length != 0, set it to 1 hour. Enable back button
                true -> {
                    chosenTime.duration = 60
                    view.toggleLength.text = chosenTime.durationAsString()
                    view.btnLengthDown.visibility = View.VISIBLE
                }

                // Length == 0, disable back button, enable forward button (when duration was max)
                false -> {
                    chosenTime.duration = 0
                    view.btnLengthDown.visibility = View.INVISIBLE
                    view.btnLengthUp.visibility = View.VISIBLE
                }
            }
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
            // Time: Set to 12:00AM, duration = 0
            chosenTime.setDefault()
            view.txtChosenTime.text = chosenTime.startTimeLabel()
            view.toggleTimeOfDay.isChecked = true
            view.toggleLength.isChecked = false

            // TimeDelta
            Settings.resetTimeDelta()
            view.btnDelta.text = Settings.timeDeltaAsString()
            view.btnLengthDown.visibility = View.INVISIBLE
            view.btnLengthUp.visibility = View.VISIBLE
        }
        view.btnClearTime.setOnClickListener {
            // edited.clear()
            edited.unset()
            modify?.text = defaultTimeMsg
            update = true
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
            update = true
            window.dismiss()
        }

        view.timeDismissLeft.setOnClickListener { window.dismiss() }
        view.timeDismissRight.setOnClickListener { window.dismiss() }

        window.show(attachTo)
        return window
    }

    private fun TextView.updateHour(toggleTimeOfDay: ToggleButton, increment: Boolean = true) {
        // Update hour then overall time display
        chosenTime.updateHour(increment)
        text = chosenTime.overallTimeLabel()
        toggleTimeOfDay.updateTimeOfDayDisplay()
    }
    private fun TextView.updateMin(increment: Boolean = true) {
        // Update min then overall time display
        chosenTime.updateMin(increment)
        text = chosenTime.overallTimeLabel()
    }
    private fun TextView.updateLength(toggleLength: ToggleButton, increment: Boolean = true) {
        chosenTime.updateDuration(increment)
        text = chosenTime.overallTimeLabel()
        toggleLength.text = chosenTime.durationAsString()
    }
    private fun TextView.updateDelta(increment: Boolean = true) {
        Settings.updateTimeDelta(increment)
        text = Settings.timeDeltaAsString()
        SaveData.saveTimeDelta(this.context)
    }

    private fun ToggleButton.updateDurationDisplay() {
        isChecked = when (chosenTime.duration) {
            0 -> false
            else -> true
        }
    }
    private fun ToggleButton.updateTimeOfDayDisplay() {
        isChecked = when (chosenTime.timeOfDay) {
            TimeOfDay.AM -> true
            TimeOfDay.PM -> false
        }
    }
}
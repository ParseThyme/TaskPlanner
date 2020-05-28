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

        // Copy over most recent time
        chosenTime = edited.copy()

        // Set text display
        // If time unset, set default values
        if (!chosenTime.isValid()) chosenTime.resetValues()

        // Check duration, prevent decrement/increment if at end caps
        when (chosenTime.duration ) {
            0 -> view.btnDurationDown.visibility = View.INVISIBLE                    // Prevent decrement
            Settings.durationMax -> view.btnDurationUp.visibility = View.INVISIBLE   // Prevent increment
        }

        // Set text display based on current time values
        view.updateDisplay()

        // Click behaviour
        // Time - Hour
        view.btnTimeHourUp.setOnClickListener { view.txtTimeHour.updateHour(view.txtTimeOfDay, view.txtTimeEnd) }
        view.btnTimeHourDown.setOnClickListener { view.txtTimeHour.updateHour(view.txtTimeOfDay, view.txtTimeEnd, false) }
        view.btnTimeHour6.setOnClickListener { view.txtTimeHour.setHour(6, view.txtTimeEnd) }
        view.btnTimeHour12.setOnClickListener { view.txtTimeHour.setHour(12, view.txtTimeEnd) }
        // Time - Min
        view.btnTimeMinUp.setOnClickListener { view.txtTimeMinute.updateMin(view.txtTimeEnd) }
        view.btnTimeMinDown.setOnClickListener { view.txtTimeMinute.updateMin(view.txtTimeEnd, false) }
        view.btnTimeMin0.setOnClickListener { view.txtTimeMinute.setMin(0, view.txtTimeEnd) }
        view.btnTimeMin30.setOnClickListener { view.txtTimeMinute.setMin(30, view.txtTimeEnd) }
        // Time - TimeOfDay
        view.txtTimeOfDay.setOnClickListener {
            chosenTime.timeOfDay = chosenTime.getOppositeTimeOfDay()
            view.txtTimeOfDay.text = chosenTime.timeOfDay.asString()
            if (chosenTime.duration > 0) view.txtTimeEnd.text = chosenTime.endTimeLabel()   // Update time end display if duration set
        }

        // Duration
        view.btnDurationUp.setOnClickListener {
            // 1. Re-enable decrements if at 0
            if (chosenTime.duration == 0) view.btnDurationDown.visibility = View.VISIBLE
            // Increment duration
            view.txtDuration.updateDuration(view.txtTimeEnd)
            // 2. Disable further increments if at max duration
            if (chosenTime.duration == Settings.durationMax) view.btnDurationUp.visibility = View.INVISIBLE
        }
        view.btnDurationDown.setOnClickListener {
            // 1. Re-enable increments if at max
            if (chosenTime.duration == Settings.durationMax) view.btnDurationUp.visibility = View.VISIBLE
            // Decrement duration
            view.txtDuration.updateDuration(view.txtTimeEnd,false)
            // 2. Disable further decrements if at 0
            if (chosenTime.duration == 0) view.btnDurationDown.visibility = View.INVISIBLE
        }
        view.btnDuration0.setOnClickListener {
            // Was max, enable duration increase
            if (chosenTime.duration == Settings.durationMax) view.btnDurationUp.visibility = View.VISIBLE
            // Set duration to 0
            view.txtDuration.setDuration(0, view.txtTimeEnd)
            view.btnDurationDown.visibility = View.INVISIBLE  // Disable duration decrease
        }
        view.btnDuration1h.setOnClickListener {
            // Check previous duration
            when (chosenTime.duration) {
                // Was max, enable duration increase
                Settings.durationMax -> view.btnDurationUp.visibility = View.VISIBLE
                // Was 0, enable duration decrease
                0 -> view.btnDurationDown.visibility = View.VISIBLE
            }
            // Set duration to 60
            view.txtDuration.setDuration(60, view.txtTimeEnd)
        }

        // Delta value
        view.btnDeltaUp.setOnClickListener { view.txtDelta.updateDelta() }
        view.btnDeltaDown.setOnClickListener { view.txtDelta.updateDelta(false) }

        // Reset, Clear Time & Confirm
        view.btnResetTime.setOnClickListener {
            chosenTime.resetValues()
            view.updateDisplay()
        }
        view.btnClearTime.setOnClickListener {
            edited.clear()
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
            modify?.text = chosenTime.startAndEndTimeLabel()
            window.dismiss()
        }
        view.timeDismissBackground.setOnClickListener { window.dismiss() }

        window.show(attachTo)
        return window
    }

    private fun TextView.updateHour(timeOfDayView: TextView, timeEndView: TextView, increment: Boolean = true) {
        // Update hour text
        chosenTime.updateHour(increment)
        text = chosenTime.hour.toString()
        // Update time of day text
        timeOfDayView.text = chosenTime.timeOfDay.asString()
        // If duration set, update time end display
        if (chosenTime.duration > 0) timeEndView.text = chosenTime.endTimeLabel()
    }
    private fun TextView.setHour(hour: Int, timeEndView: TextView) {
        chosenTime.hour = hour
        this.text = hour.toString()
        // If duration set, update time end display
        if (chosenTime.duration > 0) timeEndView.text = chosenTime.endTimeLabel()
    }

    private fun TextView.updateMin(timeEndView: TextView, increment: Boolean = true) {
        chosenTime.updateMin(increment)
        text = chosenTime.min.minutesAsString()
        // If duration set, update time end label
        if (chosenTime.duration > 0) timeEndView.text = chosenTime.endTimeLabel()
    }
    private fun TextView.setMin(minutes: Int, timeEndView: TextView) {
        chosenTime.min = minutes
        this.text = minutes.minutesAsString()
        // If duration set, update time end label
        if (chosenTime.duration > 0) timeEndView.text = chosenTime.endTimeLabel()
    }

    private fun TextView.updateDuration(endTimeLabel: TextView, increment: Boolean = true) {
        chosenTime.updateDuration(increment)
        this.text = chosenTime.durationAsString()
        endTimeLabel.text = chosenTime.endTimeLabel()
    }
    private fun TextView.setDuration(duration: Int, endTimeLabel: TextView) {
        chosenTime.duration = duration
        this.text = chosenTime.durationAsString()
        endTimeLabel.text = chosenTime.endTimeLabel()
    }

    private fun TextView.updateDelta(increment: Boolean = true) {
        Settings.updateTimeDelta(increment)
        this.text = Settings.timeDeltaAsString()
    }

    private fun View.updateDisplay() {
        // Time
        txtTimeHour.text = chosenTime.hour.toString()
        txtTimeMinute.text = chosenTime.min.minutesAsString()
        txtTimeOfDay.text = chosenTime.timeOfDay.asString()
        txtTimeEnd.text = chosenTime.endTimeLabel()
        // Duration
        txtDuration.text = chosenTime.durationAsString()
        // TimeDelta
        txtDelta.text = Settings.timeDeltaAsString()
    }
}
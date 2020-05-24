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
    private var time: TaskTime = TaskTime(12, 0, TimeOfDay.PM, 0)

    fun create(attachTo: View, modify: TextView?, context: Context, edited: TaskTime, anchor: Anchor = Anchor.Above) : PopupWindow {
        val window:PopupWindow = create(context, R.layout.popup_time)
        val view:View = window.contentView

        // Copy over most recent time
        time = edited.copy()

        // Set text display
        // If time unset, set default values
        if (!time.isValid()) time.resetValues()

        // Check duration, prevent decrement/increment if at end caps
        when (time.duration ) {
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
            time.timeOfDay = time.getOppositeTimeOfDay()
            view.txtTimeOfDay.text = time.timeOfDay.asString()
        }

        // Duration
        view.btnDurationUp.setOnClickListener {
            // 1. Re-enable decrements if at 0
            if (time.duration == 0) view.btnDurationDown.visibility = View.VISIBLE
            // Increment duration
            view.txtDuration.updateDuration(view.txtTimeEnd)
            // 2. Disable further increments if at max duration
            if (time.duration == Settings.durationMax) view.btnDurationUp.visibility = View.INVISIBLE
        }
        view.btnDurationDown.setOnClickListener {
            // 1. Re-enable increments if at max
            if (time.duration == Settings.durationMax) view.btnDurationUp.visibility = View.VISIBLE
            // Decrement duration
            view.txtDuration.updateDuration(view.txtTimeEnd,false)
            // 2. Disable further decrements if at 0
            if (time.duration == 0) view.btnDurationDown.visibility = View.INVISIBLE
        }
        view.btnDuration0.setOnClickListener {
            // Was max, enable duration increase
            if (time.duration == Settings.durationMax) view.btnDurationUp.visibility = View.VISIBLE
            // Set duration to 0
            view.txtDuration.setDuration(0, view.txtTimeEnd)
            view.btnDurationDown.visibility = View.INVISIBLE  // Disable duration decrease
        }
        view.btnDuration1h.setOnClickListener {
            // Check previous duration
            when (time.duration) {
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
            time.resetValues()
            view.updateDisplay()
        }
        view.btnClearTime.setOnClickListener {
            edited.clear()
            modify?.text = defaultTimeMsg
            window.dismiss()
        }
        view.btnApplyTime.setOnClickListener {
            edited.apply {
                hour = time.hour
                min = time.min
                duration = time.duration
                timeOfDay = time.timeOfDay
            }
            modify?.text = time.startAndEndTimeLabel()
            window.dismiss()
        }

        window.show(attachTo)
        return window
    }

    private fun TextView.updateHour(timeOfDayView: TextView, timeEndView: TextView, increment: Boolean = true) {
        // Update hour text
        time.updateHour(increment)
        text = time.hour.toString()
        // Update time of day text
        timeOfDayView.text = time.timeOfDay.asString()
        // If duration set, update time end display
        if (time.duration > 0) timeEndView.text = time.endTimeLabel()
    }
    private fun TextView.setHour(hour: Int, timeEndView: TextView) {
        time.hour = hour
        this.text = hour.toString()
        // If duration set, update time end display
        if (time.duration > 0) timeEndView.text = time.endTimeLabel()
    }

    private fun TextView.updateMin(timeEndView: TextView, increment: Boolean = true) {
        time.updateMin(increment)
        text = time.min.minutesAsString()
        // If duration set, update time end label
        if (time.duration > 0) timeEndView.text = time.endTimeLabel()
    }
    private fun TextView.setMin(minutes: Int, timeEndView: TextView) {
        time.min = minutes
        this.text = minutes.minutesAsString()
        // If duration set, update time end label
        if (time.duration > 0) timeEndView.text = time.endTimeLabel()
    }

    private fun TextView.updateDuration(endTimeLabel: TextView, increment: Boolean = true) {
        time.updateDuration(increment)
        this.text = time.durationAsString()
        endTimeLabel.text = time.endTimeLabel()
    }
    private fun TextView.setDuration(duration: Int, endTimeLabel: TextView) {
        time.duration = duration
        this.text = time.durationAsString()
        endTimeLabel.text = time.endTimeLabel()
    }

    private fun TextView.updateDelta(increment: Boolean = true) {
        Settings.updateTimeDelta(increment)
        this.text = Settings.timeDeltaAsString()
    }

    private fun View.updateDisplay() {
        // Time
        txtTimeHour.text = time.hour.toString()
        txtTimeMinute.text = time.min.minutesAsString()
        txtTimeOfDay.text = time.timeOfDay.asString()
        txtTimeEnd.text = time.endTimeLabel()
        // Duration
        txtDuration.text = time.durationAsString()
        // TimeDelta
        txtDelta.text = Settings.timeDeltaAsString()
    }
}
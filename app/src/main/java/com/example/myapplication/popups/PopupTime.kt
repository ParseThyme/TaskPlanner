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

        // Set text display based on current time values
        view.updateDisplay()

        // Click behaviour
        // Time - Hour
        view.btnTimeHourUp.setOnClickListener { view.txtTimeHour.updateHour(view.txtTimeOfDay) }
        view.btnTimeHourDown.setOnClickListener { view.txtTimeHour.updateHour(view.txtTimeOfDay, false) }
        view.btnTimeHour3.setOnClickListener { view.txtTimeHour.setHour(3) }
        view.btnTimeHour6.setOnClickListener { view.txtTimeHour.setHour(6) }
        view.btnTimeHour9.setOnClickListener { view.txtTimeHour.setHour(9) }
        view.btnTimeHour12.setOnClickListener { view.txtTimeHour.setHour(12) }
        // Time - Min
        view.btnTimeMinUp.setOnClickListener { view.txtTimeMinute.updateMin() }
        view.btnTimeMinDown.setOnClickListener { view.txtTimeMinute.updateMin(false) }
        view.btnTimeMin0.setOnClickListener { view.txtTimeMinute.setMin(0) }
        view.btnTimeMin15.setOnClickListener { view.txtTimeMinute.setMin(15)  }
        view.btnTimeMin30.setOnClickListener { view.txtTimeMinute.setMin(30) }
        view.btnTimeMin45.setOnClickListener { view.txtTimeMinute.setMin(45) }
        // Time - TimeOfDay
        view.txtTimeOfDay.setOnClickListener {
            time.timeOfDay = time.getOppositeTimeOfDay()
            view.txtTimeOfDay.text = time.timeOfDay.asString()
        }

        // Duration
        view.btnDurationUp.setOnClickListener {  }
        view.btnDurationDown.setOnClickListener {  }
        view.btnDuration15m.setOnClickListener { view.txtDuration.setDuration(15) }
        view.btnDuration30m.setOnClickListener { view.txtDuration.setDuration(30) }
        view.btnDuration1h.setOnClickListener { view.txtDuration.setDuration(60) }
        view.btnDuration2h.setOnClickListener { view.txtDuration.setDuration(120) }

        // Delta value
        view.btnDeltaUp.setOnClickListener {  }
        view.btnDeltaDown.setOnClickListener {  }

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

        /*
        // For unset times, reset value to default
        if (!time.isValid()) { view.resetValues() }

        // Apply values based on set time
        view.txtDate.text = time.createStartTime(false)
        view.txtTimeOfDay.text = time.timeOfDay
        view.txtDuration.text = time.durationToString()
        view.txtDeltaTime.text = Settings.timeDeltaAsString()

        // Allocate onClick behaviours:
        // Time and time of day
        view.btnTimeInc.setOnClickListener { view.txtDate.updateTime(view.txtTimeOfDay) }
        view.btnTimeDec.setOnClickListener { view.txtDate.updateTime(view.txtTimeOfDay, false) }
        view.txtTimeOfDay.setOnClickListener { view.txtTimeOfDay.updateTimeOfDay() }

        // Duration
        // Check duration. Hide appropriate button
        when (time.duration ) {
            0 -> view.btnLengthDec.visibility = View.INVISIBLE                      // Prevent decrement
            Settings.durationMax -> view.btnLengthInc.visibility = View.INVISIBLE   // Prevent increment
        }

        view.btnLengthInc.setOnClickListener {
            view.txtDuration.updateLength(view.btnLengthInc, view.btnLengthDec) }
        view.btnLengthDec.setOnClickListener {
            view.txtDuration.updateLength(view.btnLengthInc, view.btnLengthDec, false) }

        // Delta value, affects increment for time and duration
        view.txtDeltaTime.setOnClickListener { view.txtDeltaTime.updateDelta() }
        */

        window.show(attachTo)
        return window
    }

    private fun TextView.updateHour(timeOfDayView: TextView, increment: Boolean = true) {
        // Update hour text
        time.updateHour(increment)
        text = time.hour.toString()
        // Update time of day text
        timeOfDayView.text = time.timeOfDay.asString()
    }
    private fun TextView.setHour(hour: Int) {
        time.hour = hour
        this.text = hour.toString()
    }

    private fun TextView.updateMin(increment: Boolean = true) {
        time.updateMin(increment)
        text = time.min.minutesAsString()
    }
    private fun TextView.setMin(minutes: Int) {
        time.min = minutes
        this.text = minutes.minutesAsString()
    }

    private fun TextView.updateDuration(increment: Boolean = true) {

    }
    private fun TextView.setDuration(duration: Int) {
        time.duration = duration
        this.text = time.durationAsString()
    }

    private fun View.updateDisplay() {
        // Time
        txtTimeHour.text = time.hour.toString()
        txtTimeMinute.text = time.min.minutesAsString()
        txtTimeOfDay.text = time.timeOfDay.asString()
        txtTimeStart.text = time.startTimeLabel()
        // Duration
        txtDuration.text = time.durationAsString()
        // TimeDelta
        txtDelta.text = Settings.timeDeltaAsString()
    }

    /*
    private fun TextView.updateLength(btnUp: View, btnDown: View, increment: Boolean = true) {
        // Increment
        if (increment) {
            // Revert hiding down button, as now possible to decrement
            if (time.duration == 0) { btnDown.visibility = View.VISIBLE }

            time.duration += Settings.timeDelta

            // Ensure duration < cap, hide up button
            if (time.duration >= Settings.durationMax) {
                time.duration = Settings.durationMax
                btnUp.visibility = View.INVISIBLE
            }
        }
        // Decrement
        else {
            // Revert hiding on up button, as now possible to increment
            if (time.duration == Settings.durationMax) { btnUp.visibility = View.VISIBLE }

            time.duration -= Settings.timeDelta
            // Ensure duration >= 0, hide down button
            if (time.duration <= 0) {
                time.duration = 0
                btnDown.visibility = View.INVISIBLE
            }
        }

        // Convert duration value to String format, assign to display
        this.text = time.durationToString()
    }
    private fun TextView.updateDelta() { this.text = Settings.updateTimeDelta() }
    */
}
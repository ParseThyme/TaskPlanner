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
    private var time: TaskTime = TaskTime(12, 0, "PM", 0)

    fun create(attachTo: View, modify: TextView?, context: Context, edited: TaskTime, anchor: Anchor = Anchor.Above) : PopupWindow {
        val window:PopupWindow = create(context, R.layout.popup_time)
        val view:View = window.contentView

        // Copy over most recent time
        time = edited.copy()

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

        // Reset values
        view.btnResetDate.setOnClickListener { view.resetValues() }

        // Save updated time when window closed
        view.btnApplyTime.setOnClickListener {
            edited.apply {
                hour = time.hour
                min = time.min
                duration = time.duration
                timeOfDay = time.timeOfDay
            }
            modify?.text = time.createTimeWithDuration()
            window.dismiss()
        }

        // Clear selected time
        view.btnClearTime.setOnClickListener {
            edited.clear()
            modify?.text = defaultTimeMsg
            window.dismiss()
        }

        window.show(attachTo)
        return window
    }

    private fun View.resetValues() {
        time.resetValues()
        txtDate.text = time.createStartTime(false)
        txtTimeOfDay.text = time.timeOfDay
        txtDuration.text = time.durationToString()
        txtDeltaTime.text = "5m"
    }

    private fun TextView.updateTime(timeOfDayView: TextView, increment: Boolean = true) {
        time.update(increment)
        this.text = time.createStartTime(false)
        timeOfDayView.text = time.timeOfDay
    }
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
    private fun TextView.updateTimeOfDay() {
        // Flip time of day
        time.timeOfDay = time.getOppositeTimeOfDay()
        text = time.timeOfDay
    }
}
package com.example.myapplication.popup_windows

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.data_classes.*
import com.example.myapplication.defaultTimeMsg
import com.example.myapplication.durationMax
import kotlinx.android.synthetic.main.popup_time.view.*

class PopupTime(private val parent: Button, private val context: Context) : PopupParent() {
    var setTime: TaskTime = TaskTime()
        private set

    private var time: TaskTime = TaskTime(12, 0, "PM", 0)
    private var timeDelta: Int = 5

    fun create() : PopupWindow {
        val window:PopupWindow = createAndShow(context, R.layout.popup_time, parent)
        val view:View = window.contentView

        // Copy over most recent time
        time = setTime.copy()

        // For unset times, reset value to default
        if (!time.isValid()) {
            time.resetValues()
            view.txtDate.text = "12:00"
            view.txtTimeOfDay.text = "AM"
            view.txtDuration.text = "0m"
            view.txtDeltaTime.text = "5m"
        }

        // Apply values based on set time
        view.txtDate.text = time.asString(false)
        view.txtTimeOfDay.text = time.timeOfDay
        view.txtDuration.text = time.durationAsString()
        view.txtDeltaTime.text = deltaAsString()

        // Allocate onClick behaviours:
        // Time and time of day
        view.btnTimeInc.setOnClickListener { view.txtDate.updateTime(view.txtTimeOfDay) }
        view.btnTimeDec.setOnClickListener { view.txtDate.updateTime(view.txtTimeOfDay, false) }
        view.txtTimeOfDay.setOnClickListener { view.txtTimeOfDay.updateTimeOfDay() }

        // Duration
        // Check duration. Hide appropriate button
        when (time.duration ) {
            0 -> view.btnLengthDec.visibility = View.INVISIBLE             // Prevent decrement
            durationMax -> view.btnLengthInc.visibility = View.INVISIBLE   // Prevent increment
        }

        view.btnLengthInc.setOnClickListener {
            view.txtDuration.updateLength(view.btnLengthInc, view.btnLengthDec) }
        view.btnLengthDec.setOnClickListener {
            view.txtDuration.updateLength(view.btnLengthInc, view.btnLengthDec, false) }

        // Delta value, affects increment for time and duration
        view.txtDeltaTime.setOnClickListener { view.txtDeltaTime.updateDelta() }

        // Reset values
        view.btnResetDate.setOnClickListener {
            time.resetValues()
            view.txtDate.text = "12:00"
            view.txtTimeOfDay.text = "AM"
            view.txtDuration.text = "0m"
            view.txtDeltaTime.text = "5m"
        }

        // Save updated time when window closed
        view.btnApplyTime.setOnClickListener {
            window.dismiss()
            setTime = time
            parent.text = setTime.createDisplayedTime()
        }

        // Clear selected time
        view.btnClearTime.setOnClickListener {
            window.dismiss()
            setTime.hour = 0
            parent.text = defaultTimeMsg
        }

        return window
    }

    private fun TextView.updateTime(timeOfDayView: TextView, increment: Boolean = true) {
        var newMinutes: Int = time.min
        var hourDelta = 0
        var flipToD = false

        if (increment) {
            newMinutes += timeDelta

            // Result is a number over 60
            if (newMinutes > 59) {
                // Calculate how many hours we need to add to time and add it
                hourDelta = newMinutes / 60
                // Ensure time between 0-60
                newMinutes %= 60
            }
        }
        else {
            newMinutes -= timeDelta

            // Result is number under 0 minutes
            if (newMinutes < 0) {
                // Ensure time in appropriate range
                newMinutes += 60
                // Calculate hours required to subtract
                hourDelta = -((newMinutes + 60) / 60)
            }
        }

        // Assuming hour has been updated, make sure result is in range
        if (hourDelta != 0) {
            val hourResult = time.hour + hourDelta

            when {
                // Values > 12, reset back to 1
                hourResult > 12 -> {
                    time.hour = 1
                    flipToD = true
                }
                // Values < 1, reset to 12
                hourResult < 1 -> {
                    time.hour = 12
                    flipToD = true
                }
                // Standard hour increment/decrement. Value between 1-12
                else -> time.hour = hourResult
            }

            if (flipToD) {
                // Flip time of day
                time.timeOfDay = time.getOppositeTimeOfDay()
                timeOfDayView.text = time.timeOfDay
            }
        }

        // Update minute value
        time.min = newMinutes

        // Show new displayed time
        this.text = time.asString(false)
    }
    private fun TextView.updateLength(btnUp: View, btnDown: View, increment: Boolean = true) {
        // Increment
        if (increment) {
            // Revert hiding down button, as now possible to decrement
            if (time.duration == 0) { btnDown.visibility = View.VISIBLE }

            time.duration += timeDelta

            // Ensure duration < cap, hide up button
            if (time.duration >= durationMax) {
                time.duration = durationMax
                btnUp.visibility = View.INVISIBLE
            }
        }
        // Decrement
        else {
            // Revert hiding on up button, as now possible to increment
            if (time.duration == durationMax) { btnUp.visibility = View.VISIBLE }

            time.duration -= timeDelta
            // Ensure duration >= 0, hide down button
            if (time.duration <= 0) {
                time.duration = 0
                btnDown.visibility = View.INVISIBLE
            }
        }

        // Convert duration value to String format, assign to display
        this.text = time.durationAsString()
    }
    private fun TextView.updateDelta() {
        when (timeDelta) {
            5 -> timeDelta = 10
            10 -> timeDelta = 15
            15 -> timeDelta = 30
            30 -> timeDelta = 60
            60 -> timeDelta = 5
        }
        // Replace string with 1h if 60 minutes, otherwise append on m for minute values
        this.text = deltaAsString()
    }
    private fun TextView.updateTimeOfDay() {
        // Flip time of day
        time.timeOfDay = time.getOppositeTimeOfDay()
        text = time.timeOfDay
    }

    private fun deltaAsString(): String {
        var result:String = timeDelta.toString()
        if (timeDelta == 60) result = "1h"
        else result += "m"
        return result
    }
}
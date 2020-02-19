package com.example.myapplication.popup_windows

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.data_classes.*
import com.example.myapplication.defaultTimeMsg
import com.example.myapplication.durationMax
import kotlinx.android.synthetic.main.popup_time.view.*

class PopupTime(private val parent: Button, private val context: Context) : PopupWindowParent() {
    var setTime: TaskTime = TaskTime()
        private set

    private var time: TaskTime = TaskTime(12, 0, "AM", 0)
    private var timeDelta: Int = 5

    fun create() : PopupWindow {
        val window:PopupWindow = createAndShow(context, R.layout.popup_time, parent)
        val view:View = window.contentView

        // Copy over most recent time
        time = setTime.copy()

        // For unset times, reset value to default
        if (!time.isValid()) { window.resetValues() }

        // Apply values based on set time
        view.txtTime.text = time.asString(false)
        view.txtTimeOfDay.text = time.timeOfDay
        view.txtDuration.text = time.durationAsString()
        view.txtIncDelta.text = deltaAsString()

        // Allocate onClick behaviours:
        // Time and time of day
        view.btnTimeUp.setOnClickListener { view.txtTime.updateTime() }
        view.btnTimeDown.setOnClickListener { view.txtTime.updateTime(false) }
        view.txtTimeOfDay.setOnClickListener { view.txtTimeOfDay.changeTimeOfDay() }

        // Duration
        view.btnDurationUp.setOnClickListener { view.txtDuration.updateDuration() }
        view.btnDurationDown.setOnClickListener { view.txtDuration.updateDuration(false) }

        // Delta value, affects increment for time and duration
        view.txtIncDelta.setOnClickListener { view.txtIncDelta.updateDelta() }

        // Reset values
        view.btnResetTime.setOnClickListener { window.resetValues() }

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

    private fun TextView.changeTimeOfDay() {
        val newToD: String = when (text) {
            "AM" -> "PM"
            else -> "AM"
        }

        time.timeOfDay = newToD
        text = newToD
    }

    private fun TextView.updateTime(increment: Boolean = true) {
        var newMinutes: Int = time.min
        var hourDelta = 0

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

            // Result is number 0 minutes
            if (newMinutes < 0) {
                // Calculate hours required to subtract
                hourDelta = -((60 - newMinutes) / 60)
                // Ensure time in appropriate range
                newMinutes += 60
            }
        }

        // Assuming hour has been updated, make sure result is in range
        if (hourDelta != 0) {
            val hourResult = time.hour + hourDelta

            time.hour = when {
                // Values > 12, reset back to 1
                hourResult > 12 -> 1
                // Values < 1, reset to 12
                hourResult < 1 -> 12
                // Standard hour increment/decrement. Value between 1-12
                else -> hourResult
            }
        }

        // Update minute value
        time.min = newMinutes

        // Show new displayed time
        this.text = time.asString(false)
    }

    private fun TextView.updateDuration(increment: Boolean = true) {
        // Increment
        if (increment) {
            time.duration += timeDelta
            // Ensure duration < cap
            if (time.duration > durationMax) { time.duration = 0 }
        }
        // Decrement
        else {
            time.duration -= timeDelta
            // Ensure duration >= 0
            if (time.duration < 0) { time.duration = durationMax }
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

    private fun deltaAsString(): String {
        var result:String = timeDelta.toString()
        if (timeDelta == 60) result = "1h"
        else result += "m"
        return result
    }

    private fun PopupWindow.resetValues() {
        val view:View = this.contentView

        time.resetValues()
        view.txtTime.text = "12:00"
        view.txtTimeOfDay.text = "AM"
        view.txtDuration.text = "0m"
        view.txtIncDelta.text = "5m"
    }
}
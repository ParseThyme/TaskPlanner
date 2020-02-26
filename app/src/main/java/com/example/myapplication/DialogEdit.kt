package com.example.myapplication

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.Window.FEATURE_NO_TITLE
import com.example.myapplication.data_classes.*
import com.example.myapplication.popup_windows.*
import kotlinx.android.synthetic.main.task_edit_view.view.*


class DialogEdit(
    private val context: Context,
    private val updateSave: () -> Unit
) {
    var updated: Boolean = false
        private set

    fun create(
        date: TaskDate,
        task: Task,
        notifyItemChanged : () -> Unit,
        changeGroup: (Task, TaskDate, Int) -> Unit
    ): Dialog {
        updated = false

        // ########## Create dialog ##########
        // https://demonuts.com/android-custom-dialog-with-transparent-background/
        val dialog = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar)
        val view: View = LayoutInflater.from(context).inflate(R.layout.task_edit_view, null)
        dialog.apply {
            requestWindowFeature(FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(view)
            show()
        }

        // Create copy of task and date to store changes. Only apply when applyChanges button clicked
        val taskData: Task = task.copy()
        val dateData: TaskDate = date.copy()

        // 1. Store copy of setTime
        // 2. Modify setTime to be copy of task's time
        // 3. Create window and change value to new
        // 4. Reassign setTime back to copy

        // ########## Fill values: ##########
        // Description
        view.txtEditDesc.apply {
            // Set text and hint text to description
            setText(task.desc)
            hint = task.desc
            // Close keyboard when editText loses focus
            closeKeyboardOnFocusLost()
        }

        // Tag
        view.iconEditTag.setImageResourceFromTag(task.tag)
        view.iconEditTag.setOnClickListener {
            PopupManager.tagPopup(view.windowLayout, view.iconEditTag, context, taskData)
        }

        // Date
        view.txtEditDate.text = date.createLabel(Size.Med)
        view.txtEditDate.setOnClickListener {
            PopupManager.datePopup(view.windowLayout, view.txtEditDate, context, dateData)
        }

        // Time
        view.txtEditTime.text = task.time.createDisplayedTime()
        view.txtEditTime.setOnClickListener {
            PopupManager.timePopup(view.windowLayout, view.txtEditTime, context, taskData)
        }

        // Reset settings
        view.btnReset.setOnClickListener { }

        // Close Dialog. Cancel changes made to data.
        view.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // Apply Changes
        view.btnApply.setOnClickListener {
            // Check if changes have been made. If yes, then apply changes to task
            // 1. Time
            if (task.time != taskData.time) {
                updated = true
                task.time = taskData.time
            }

            // 2. Tag
            if (task.tag != taskData.tag) {
                updated = true
                task.tag = taskData.tag
            }

            if (date != dateData) {
                updated = true
                changeGroup(task, dateData, date.id)
            }

            // Save changes made and update recyclerview display
            if (updated) {
                notifyItemChanged()
                updateSave()
            }

            dialog.dismiss()
        }

        return dialog
    }
}
package com.example.myapplication

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.example.myapplication.data_classes.*
import com.example.myapplication.popup_windows.PopupManager
import kotlinx.android.synthetic.main.task_edit_view.view.*


class DialogEdit(
    private val context: Context,
    private val updateSave: () -> Unit
) {
    var updated: Boolean = false
        private set
    private var keyBoardOpen = false

    fun create(
        date: TaskDate,
        task: Task,
        notifyItemChanged : () -> Unit,
        changeGroup: (Task, TaskDate, Int) -> Unit
    ): Dialog {
        updated = false
        keyBoardOpen = false

        // ########## Create dialog ##########
        // https://demonuts.com/android-custom-dialog-with-transparent-background/
        /*
        val dialog = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar)
        val view: View = LayoutInflater.from(context).inflate(R.layout.task_edit_view, null)
        dialog.apply {
            //requestWindowFeature(FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(view)
            show()
        }
        */

        val dialog = Dialog(context, R.style.EditDialog)
        val view: View = LayoutInflater.from(context).inflate(R.layout.task_edit_view, null)
        dialog.apply {
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
        view.iconKeyboard.setOnClickListener {
            keyBoardOpen = !keyBoardOpen

            when (keyBoardOpen) {
                // Open keyboard, enable text editing
                true -> {
                    view.iconKeyboard.setImageResource(R.drawable.ic_keyboard_hide)
                    view.txtEditDesc.requestFocus()
                    view.txtEditDesc.showKeyboard()
                }
                // Close keyboard and lose focus on edit text
                else -> {
                    view.iconKeyboard.setImageResource(R.drawable.ic_keyboard)
                    view.txtEditDesc.hideKeyboard()
                }
            }
        }
        view.txtEditDesc.apply {
            // Set text and hint text to description
            setText(task.desc)
            hint = task.desc
            // Toggling focus on text
            setOnFocusChangeListener { _, focused ->
                // Close keyboard when editText loses focus
                if (!focused) {
                    keyBoardOpen = false
                    this.hideKeyboard()
                    view.iconKeyboard.setImageResource(R.drawable.ic_keyboard)
                }
            }

            setOnClickListener {
                // When keyboard isn't open, change icon when keyboard opened by system
                if (!keyBoardOpen) {
                    keyBoardOpen = true
                    view.iconKeyboard.setImageResource(R.drawable.ic_keyboard_hide)
                }
            }
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
            // Hide keyboard if open then close dialog
            if (keyBoardOpen) view.txtEditDesc.hideKeyboard()
            dialog.dismiss()
        }

        // Apply Changes
        view.btnApply.setOnClickListener {
            // Check for open keyboard
            if (keyBoardOpen) view.txtEditDesc.hideKeyboard()

            // Check if changes have been made. If yes, then apply changes to task
            // Description
            val description: String = view.txtEditDesc.text.trim().toString()
            if (description != "" && description != task.desc) {
                updated = true
                task.desc = description
            }

            // Time
            if (task.time != taskData.time) {
                updated = true
                task.time = taskData.time
            }

            // Tag
            if (task.tag != taskData.tag) {
                updated = true
                task.tag = taskData.tag
            }

            // Date
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
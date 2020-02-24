package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.example.myapplication.data_classes.*
import com.example.myapplication.popup_windows.PopupDate
import com.example.myapplication.popup_windows.PopupTag
import com.example.myapplication.popup_windows.PopupTime
import kotlinx.android.synthetic.main.task_edit_activity_view.view.*

class AlertDialogEdit(private val context: Context) {

    fun create(group: TaskGroup, task: Task) {
        // Set view to be applied to alert dialog
        val view: View = LayoutInflater.from(context).inflate(R.layout.task_edit_activity_view, null)
        // Create builder
        val builder = AlertDialog.Builder(context).apply {
            setView(view)
            // setCancelable(false)
        }

        // https://stackoverflow.com/questions/2306503/how-to-make-an-alert-dialog-fill-90-of-screen-size
        val dialog: AlertDialog = builder.create()

        // Dialog sizing
        val layout: WindowManager.LayoutParams = WindowManager.LayoutParams()
        layout.apply {
            copyFrom(dialog.window?.attributes)
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        dialog.show()
        dialog.window?.attributes = layout

        // Show dialog
        // val dialog: AlertDialog = builder.show()

        // Popups
        // datePopup = PopupDate(view.btnEditDate, view.context)
        // tagPopup = PopupTag(view.btnSetTag, view.context)

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
        view.iconEditTag.setImageResourceFromTag(task.tag)                                  // Set current
        // view.iconEditTag.setOnClickListener { PopupTag.create(view.iconEditTag, context) }  // Switch

        // Date
        view.txtEditDate.text = group.date.createLabel(Size.Med)
        // view.txtEditDate.setOnClickListener { PopupDate.create(view.txtEditDate, context) }

        // Time
        view.txtEditTime.text = task.time.asString()
        // view.txtEditTime.setOnClickListener { PopupTime.create(view.txtEditTime, context) }

        // Close Dialog
        // view.btnCancel.setOnClickListener { dialog.dismiss() }

        // Apply Changes
    }
}
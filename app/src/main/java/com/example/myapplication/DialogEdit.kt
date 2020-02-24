package com.example.myapplication

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.Window.FEATURE_NO_TITLE
import com.example.myapplication.data_classes.*
import com.example.myapplication.popup_windows.PopupDate
import com.example.myapplication.popup_windows.PopupTag
import com.example.myapplication.popup_windows.PopupTime
import kotlinx.android.synthetic.main.task_edit_view.view.*


class DialogEdit(private val context: Context) {

    fun create(group: TaskGroup, task: Task) {
        // https://demonuts.com/android-custom-dialog-with-transparent-background/
        val dialog = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
        val view: View = LayoutInflater.from(context).inflate(R.layout.task_edit_view, null)
        dialog.apply {
            requestWindowFeature(FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(view)
            show()
        }

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
        view.iconEditTag.setOnClickListener { PopupTag.create(view.windowLayout, view.iconEditTag, context) }

        // Date
        view.txtEditDate.text = group.date.createLabel(Size.Med)
        view.txtEditDate.setOnClickListener { PopupDate.create(view.windowLayout, view.txtEditDate, context) }

        // Time
        view.txtEditTime.text = task.time.asString()
        view.txtEditTime.setOnClickListener { PopupTime.create(view.windowLayout, view.txtEditTime, context) }

        // Reset settings
        view.btnReset.setOnClickListener {  }

        // Close Dialog

        // Apply Changes
        view.btnApply.setOnClickListener {
            dialog.dismiss()
        }
    }
}
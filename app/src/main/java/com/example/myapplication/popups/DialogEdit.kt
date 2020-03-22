package com.example.myapplication.popups

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.example.myapplication.R
import com.example.myapplication.data_classes.*
import com.example.myapplication.utility.Keyboard
import com.example.myapplication.utility.printDebugMsg
import kotlinx.android.synthetic.main.task_edit_view.view.*


class DialogEdit(
    private val context: Context,
    private val updateSave: () -> Unit
) {
    var updated: Boolean = false
        private set

    fun create(
        task: Task,
        notifyItemChanged: () -> Unit
    ): Dialog {
        updated = false

        // ########## Create dialog ##########
        // https://demonuts.com/android-custom-dialog-with-transparent-background/
        val dialog = Dialog(context,
            R.style.EditDialog
        )
        val view: View = LayoutInflater.from(context).inflate(R.layout.task_edit_view, null)
        dialog.apply {
            setCancelable(false)
            setContentView(view)
            show()
        }

        // ########## Fill values: ##########
        // Description
        // Create keyboard reference
        Keyboard.attachTo(view.txtEditDesc)
        // Set text and hint text to description
        view.txtEditDesc.setText(task.desc)

        // Reset settings, place cursor at bottom
        view.btnReset.setOnClickListener {
            view.txtEditDesc.setText(task.desc)
            view.txtEditDesc.setSelection(view.txtEditDesc.text.length)
        }

        // Place cursor at bottom
        view.btnDescription.setOnClickListener {
            view.txtEditDesc.setSelection(view.txtEditDesc.text.length)
        }

        // Close Dialog. Cancel changes made to data.
        view.btnClose.setOnClickListener {
            // Hide keyboard if open then close dialog
            Keyboard.close()
            dialog.dismiss()
        }

        // Apply Changes
        view.btnApply.setOnClickListener {
            // Check if changes have been made. If yes, then apply changes to task
            // Description
            val description: String = view.txtEditDesc.text.trim().toString()
            if (description != "" && description != task.desc) {
                task.desc = description

                // Save changes made and update recyclerview display
                notifyItemChanged()
                updateSave()
            }

            Keyboard.close()
            dialog.dismiss()
        }

        return dialog
    }
}
package com.example.myapplication.popups

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data_classes.Task
import com.example.myapplication.data_classes.assignTag
import com.example.myapplication.inflate
import kotlinx.android.synthetic.main.popup_stored_task_entry.view.*
import kotlinx.android.synthetic.main.popup_stored_tasks.view.*
import kotlinx.android.synthetic.main.task_entry_rv.view.*

class PopupSavedTasks : Popup() {
    var modified = false

    fun create(attachTo: View, modify: TextView?,
               edited: Task, storedTasks: ArrayList<Task>): PopupWindow
    {
        val window: PopupWindow = create(attachTo.context, R.layout.popup_stored_tasks)
        val view: View = window.contentView

        modified = false

        // RecyclerView
        val savedTasksLayoutManager = LinearLayoutManager(attachTo.context)
        savedTasksLayoutManager.stackFromEnd = true                 // Place in reverse order
        view.savedTasksRV.apply {
            layoutManager = savedTasksLayoutManager
            adapter = PopupSavedTaskAdapter(storedTasks)
            // Select and close function passed into TaskTagAdapter
            { chosenTask: Task? ->           // Input Param
                if (chosenTask != null) {
                    modify?.text = chosenTask.desc
                    edited.desc = chosenTask.desc
                }
                window.dismiss()
            }
        }
        // Dismiss window
        view.savedTasksDismissLeft.setOnClickListener { window.dismiss() }
        view.savedTasksDismissRight.setOnClickListener { window.dismiss() }

        window.show(attachTo)
        return window
    }

    inner class PopupSavedTaskAdapter (private val taskList: ArrayList<Task>,
                                       private val closeFn: (Task?) -> Unit
    )
        : RecyclerView.Adapter<PopupSavedTaskAdapter.ViewHolder>() {
        override fun getItemCount(): Int { return taskList.size }
        override fun onBindViewHolder(holder: ViewHolder, pos: Int) { holder.bind(taskList[pos]) }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent.inflate(R.layout.popup_stored_task_entry))
        }

        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            fun bind(task: Task) {
                // Set text and click listener when text clicked
                itemView.txtStoredTask.text = task.desc
                itemView.txtStoredTask.setOnClickListener { closeFn(task) }

                // Set tag - Hide if base tag
                itemView.tagStoredTask.assignTag(task.tag)

                // Listener when delete button clicked
                itemView.btnDeleteStoredTask.setOnClickListener {
                    modified = true
                    taskList.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)

                    // Close window if all saved tasks were removed (none selected)
                    if (taskList.size == 0) {
                        closeFn(null)
                    }
                }
            }
        }
    }
}
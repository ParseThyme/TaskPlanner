package com.example.myapplication.popups

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data_classes.Task
import com.example.myapplication.utility.inflate
import kotlinx.android.synthetic.main.popup_saved_task_entry.view.*
import kotlinx.android.synthetic.main.popup_saved_tasks.view.*

class PopupSavedTasks(private val savedTasks: ArrayList<String>) : Popup() {

    fun create(attachTo: View, modify: TextView?, context: Context, edited: Task): PopupWindow {
        val window: PopupWindow = create(context, R.layout.popup_saved_tasks)
        val view: View = window.contentView

        // RecyclerView
        val savedTasksLayoutManager = LinearLayoutManager(context)
        savedTasksLayoutManager.stackFromEnd = true                 // Place in reverse order
        view.savedTasksRV.apply {
            layoutManager = savedTasksLayoutManager
            adapter = PopupSavedTaskAdapter(savedTasks)
            // Select and close function passed into TaskTagAdapter
            { chosenTask: String ->           // Input Param
                modify?.text = chosenTask
                edited.desc = chosenTask
                window.dismiss()
            }
        }
        // If < 5 tasks, wrap content (otherwise default is capped height)
        if (savedTasks.size < 5) {
            val params: ViewGroup.LayoutParams = view.savedTasksRV.layoutParams
            params.height = LinearLayout.LayoutParams.WRAP_CONTENT
            view.savedTasksRV.layoutParams = params
        }
        // Dismiss window
        view.savedTasksDismissLeft.setOnClickListener { window.dismiss() }
        view.savedTasksDismissRight.setOnClickListener { window.dismiss() }

        window.show(attachTo)
        return window
    }
}

class PopupSavedTaskAdapter (private val taskList: ArrayList<String>, private val closeFn: (String) -> Unit)
    : RecyclerView.Adapter<PopupSavedTaskAdapter.ViewHolder>() {
    override fun getItemCount(): Int { return taskList.size }
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) { holder.bind(taskList[pos]) }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.popup_saved_task_entry))
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(entry: String) {
            itemView.txtSavedTask.apply {
                text = entry
                setOnClickListener { closeFn(entry) }
            }
        }
    }
}
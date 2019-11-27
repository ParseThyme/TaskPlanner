package com.example.myapplication

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.AdapterTaskGroup.*
import kotlinx.android.synthetic.main.rv_taskgroup.view.*

class ViewHolderTaskGroup(itemView: View, private val clickListener: ClickListener):
    RecyclerView.ViewHolder(itemView), View.OnClickListener
{
    // Defining reference to task description text in layout
    private val tasksRV = itemView.taskGroupRV
    private val dateLabel = itemView.dateLabel

    // private val selectedIcon = view.selected

    private lateinit var taskAdapter: AdapterTasks

    fun bind(taskGroup: TaskGroup) {
        dateLabel.text = taskGroup.date

        // Toggle date visibility
        // toggleSelected(newTask.selected)

        // Store reference to task adapter and assign its layout manager + adapter
        taskAdapter = AdapterTasks(taskGroup.tasks)
        tasksRV.apply {
            layoutManager = LinearLayoutManager(tasksRV.context, RecyclerView.VERTICAL, false)
            adapter = taskAdapter
        }
    }

    // ########## onClick functionality/variables ##########
    init { itemView.setOnClickListener(this) }
    override fun onClick(v: View) { clickListener.onClick(adapterPosition, v) }

    // ########## Toggling functionality ##########
    /*
    private fun toggleSelected(isSelected: Boolean) {
        if (isSelected)
            selectedIcon.visibility = View.VISIBLE
        else
            selectedIcon.visibility = View.GONE
    }
    */
}
package com.example.myapplication.adapters

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data_classes.Task
import com.example.myapplication.data_classes.TaskGroup
import com.example.myapplication.inflate
import kotlinx.android.synthetic.main.rv_taskentry.view.*

// val itemClickedListener: (Task) -> Unit
// Code above takes in a lambda function as a parameter
// Unit == no return type (same as void)

class AdapterTasks(private val group: TaskGroup, private val clickListener: (Task) -> Unit) :
    RecyclerView.Adapter<AdapterTasks.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =  parent.inflate(R.layout.rv_taskentry, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int { return group.taskList.size }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(group.taskList[position], clickListener)
    }

    // ########## ViewHolder ##########
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        private val selectedIcon = itemView.selected

        fun bind(task: Task, clickListener: (Task) -> Unit) {
            itemView.desc.text = task.desc

            // Toggle selected icon
            toggleSelected(task.selected)

            this.itemView.setOnClickListener {
                // When clicked, swap its state and select/deselect it
                task.selected = !task.selected
                toggleSelected(task.selected)
                notifyItemChanged(adapterPosition)

                // If update count in group to notify number selected
                if (task.selected) {
                    group.numSelected++
                    if (group.numSelected == group.taskList.size)
                        group.allSelected = true
                } else {
                    if (group.numSelected == group.taskList.size)
                        group.allSelected = false

                    group.numSelected--
                }
                Log.d("Test", "${group.date} = [${group.numSelected}]")

                // Call main click listener function (implemented in main activity)
                clickListener(task)
            }
        }

        // ########## Toggling functionality ##########
        private fun toggleSelected(isSelected: Boolean) {
            if (isSelected)
                selectedIcon.visibility = View.VISIBLE
            else
                selectedIcon.visibility = View.GONE
        }
    }
}
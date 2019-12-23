package com.example.myapplication.adapters

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.Settings
import com.example.myapplication.data_classes.Task
import com.example.myapplication.data_classes.TaskGroup
import com.example.myapplication.inflate
import kotlinx.android.synthetic.main.task_entry_rv.view.*

// val itemClickedListener: (Task) -> Unit
// Code above takes in a lambda function as a parameter
// Unit == no return type (same as void)

class AdapterTasks(private val group: TaskGroup,
                   private val clickListener: (Task) -> Unit,
                   private val settings: Settings)
    : RecyclerView.Adapter<AdapterTasks.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =  parent.inflate(R.layout.task_entry_rv, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int { return group.taskList.size }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(group.taskList[position], clickListener)
    }

    // ########## ViewHolder ##########
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        // private val selectedIcon = itemView.selected
        private val card = itemView.card
        private val text = itemView.desc

        fun bind(task: Task, clickListener: (Task) -> Unit) {
            itemView.desc.text = task.desc

            // Toggle selected icon
            toggleSelected(task.selected)

            // When held down
            /* ToDo
            this.itemView.setOnLongClickListener {
                // Open edit dialogue
                // Enable changing task's description & date
            }
            */

            // Task Clicked
            this.itemView.setOnClickListener {
                // When clicked, swap its state and select/deselect it
                task.selected = !task.selected
                toggleSelected(task.selected)
                notifyItemChanged(adapterPosition)

                // If update count in group to notify number selected
                if (task.selected)
                    group.numSelected++
                else
                    group.numSelected--

                // Call main click listener function (implemented in main activity)
                clickListener(task)
            }
        }

        // ########## Toggling functionality ##########
        private fun toggleSelected(isSelected: Boolean) {
            /*
            if (isSelected)
                selectedIcon.visibility = View.VISIBLE
            else
                selectedIcon.visibility = View.GONE
            */

            if (isSelected) {
                card.setCardBackgroundColor(settings.taskSelectedBGColor)
                text.setTextColor(settings.taskSelectedTextColor)
            }
            else {
                card.setCardBackgroundColor(Color.WHITE)
                text.setTextColor(Color.BLACK)
            }
        }
    }
}
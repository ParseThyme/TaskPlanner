package com.example.myapplication.adapters

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.*
import com.example.myapplication.data_classes.*
import com.example.myapplication.popups.DialogEdit
import com.example.myapplication.singletons.AppData
import com.example.myapplication.singletons.Keyboard
import com.example.myapplication.singletons.Settings
import kotlinx.android.synthetic.main.task_entry_rv.view.*

// val itemClickedListener: (Task) -> Unit
// Code above takes in a lambda function as a parameter
// Unit == no return type (same as void)

class TasksAdapter(private val group: TaskGroup,
                   private val groupIndex: Int,
                   private val taskClicked: (Boolean, Int, Int) -> Unit,
                   private val updateSave: (Context) -> Unit)
    : RecyclerView.Adapter<TasksAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.task_entry_rv, false))
    }
    override fun getItemCount(): Int { return group.taskList.size }
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) { holder.bind(group.taskList[pos]) }

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        private val taskField: TextView = itemView.desc
        private val editWindow = DialogEdit(itemView.context, updateSave)

        fun bind(task: Task) {
            // Set description of task when bound
            taskField.text = task.desc

            // Show/hide tag/time if allocated
            itemView.taskTag.assignTag(task.tag)
            toggleTime(task.time)

            // Toggle selected icon based on state
            toggleSelected(task.selected)

            // Edit button
            itemView.editBtn.setOnClickListener { editTask(task) }

            // Task Clicked
            itemView.setOnClickListener {
                // When clicked, swap its state and select/deselect it
                task.selected = !task.selected
                toggleSelected(task.selected)
                notifyItemChanged(adapterPosition)

                // If update count in group to notify number selected
                when (task.selected) {
                    true -> {
                        group.numSelected++
                        AppData.numSelected++
                    }
                    false -> {
                        group.numSelected--
                        AppData.numSelected--
                    }
                }

                // Call main click listener function (implemented in main activity)
                taskClicked(task.selected, groupIndex, adapterPosition)
            }
        }

        private fun editTask(task: Task) {
            val previousEditText: EditText = Keyboard.editText      // Previous editText attached to Keyboard
            // Create new dialog
            val dialog: Dialog = editWindow.create(task)
                                 { notifyItemChanged(adapterPosition) }     // Update display when task updated

            // Reattach old editText when done editing task
            dialog.setOnDismissListener { Keyboard.attachTo(previousEditText) }
        }

        private fun toggleSelected(isSelected: Boolean) {
            when (isSelected) {
                 true -> taskField.applyBackgroundColor(Settings.highlightColor)
                false -> taskField.applyBackgroundColor(Settings.taskBaseColor)
            }
        }

        private fun toggleTime(time: TaskTime) {
            when (time.isUnset()) {
                // Time unallocated, hide display
                true -> itemView.taskTime.visibility = View.GONE
                // Otherwise set time and show
                false -> {
                    itemView.taskTime.text = time.overallTimeLabel()
                    itemView.taskTime.visibility = View.VISIBLE
                }
            }
        }
    }
}
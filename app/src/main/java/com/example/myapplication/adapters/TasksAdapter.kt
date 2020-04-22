package com.example.myapplication.adapters

import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.*
import com.example.myapplication.data_classes.*
import com.example.myapplication.popups.DialogEdit
import com.example.myapplication.utility.*
import kotlinx.android.synthetic.main.task_entry_rv.view.*

// val itemClickedListener: (Task) -> Unit
// Code above takes in a lambda function as a parameter
// Unit == no return type (same as void)

class TasksAdapter(private val group: TaskGroup,
                   private val taskClicked: (SelectedTask) -> Unit,
                   private val updateSave: () -> Unit)
    : RecyclerView.Adapter<TasksAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =  parent.inflate(R.layout.task_entry_rv, false)
        return ViewHolder(v)
    }
    override fun getItemCount(): Int { return group.taskList.size }
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) { holder.bind(group.taskList[pos]) }

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        private val taskField = itemView.desc
        private val editWindow = DialogEdit(itemView.context, updateSave)

        fun bind(task: Task) {
            val selectedTask =
                SelectedTask(
                    task.group,
                    adapterPosition
                )

            // Set description of task when bound
            taskField.text = task.desc

            // Show/hide tag/time if allocated
            toggleTag(task.tag)
            toggleTime(task.time)

            // Toggle selected icon based on state
            toggleSelected(selectedTask)

            // Edit button
            itemView.editBtn.setOnClickListener { editTask(task) }

            // Task Clicked
            itemView.setOnClickListener {
                // When clicked, swap its state and select/deselect it
                Tracker.toggle(selectedTask)
                toggleSelected(selectedTask)
                notifyItemChanged(adapterPosition)

                // If update count in group to notify number selected
                if (Tracker.isSelected(selectedTask)) group.numSelected++
                else group.numSelected--

                // Call main click listener function (implemented in main activity)
                taskClicked(selectedTask)
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

        /*
        private fun toggleSelected(isSelected: Boolean) {
            if (isSelected) { taskField.applyBackgroundColor(Settings.highlightColor) }
            else { taskField.applyBackgroundColor(Settings.taskBaseColor) }
        }
        */

        private fun toggleSelected(task: SelectedTask) {
            val isSelected: Boolean = Tracker.isSelected(task)

            if (isSelected) { taskField.applyBackgroundColor(Settings.highlightColor) }
            else { taskField.applyBackgroundColor(Settings.taskBaseColor) }
        }

        private fun toggleTag(tag: Int) {
            // No tag, don't display anything
            if (tag == R.drawable.tag_base) { itemView.taskTag.visibility = View.GONE }
            // Get image, set tag accordingly and display
            else {
                itemView.taskTag.setImageResource(tag)
                itemView.taskTag.visibility = View.VISIBLE
            }
        }

        private fun toggleTime(time: TaskTime) {
            // Time unallocated, hide display
            if (!time.isValid()) {
                itemView.taskTime.visibility = View.GONE
                return
            }

            // Otherwise set time and show
            itemView.taskTime.text = time.createTimeWithDuration()
            itemView.taskTime.visibility = View.VISIBLE
        }
    }
}
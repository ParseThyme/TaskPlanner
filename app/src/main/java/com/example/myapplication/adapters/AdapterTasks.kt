package com.example.myapplication.adapters

import android.app.AlertDialog
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.Settings
import com.example.myapplication.data_classes.Task
import com.example.myapplication.data_classes.TaskGroup
import com.example.myapplication.inflate
import com.example.myapplication.validateInput
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.task_edit_alertdialog.view.*
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

            // Edit button
            this.itemView.editBtn.setOnClickListener {
                // Set view to be applied to alert dialog
                val taskEditView = LayoutInflater.from(itemView.context).
                    inflate(R.layout.task_edit_alertdialog, null)
                // Create builder
                val taskEditBuilder = AlertDialog.Builder(itemView.context).apply {
                    setView(taskEditView)
                    setCancelable(false)
                }

                // Show dialog
                val taskEditDialog = taskEditBuilder.show()

                // Set current task as hint text
                taskEditView.task.hint = itemView.desc.text

                // Input Validation:
                // Same logic as found in ActivityMain (input validation)
                if (validateInput) {
                    taskEditView.applyBtn.isEnabled = false
                    taskEditView.applyBtn.setColorFilter(Color.GRAY)

                    taskEditView.task.addTextChangedListener(object: TextWatcher {
                        override fun afterTextChanged(s: Editable) { }
                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                        // Check when text is being changed
                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                            // Toggle confirm button based on whether text is empty or not
                            taskEditView.applyBtn.isEnabled = taskEditView.task.text.isNotEmpty()

                            if (taskEditView.applyBtn.isEnabled)
                                taskEditView.applyBtn.setColorFilter(Color.GREEN)
                            else
                                taskEditView.applyBtn.setColorFilter(Color.GRAY)
                        }
                    })
                }

                // Cancel button, close dialog
                taskEditView.cancelBtn.setOnClickListener {
                    taskEditDialog.dismiss()
                }

                // Apply button, apply changes
                taskEditView.applyBtn.setOnClickListener {
                    taskEditDialog.dismiss()
                    task.desc = taskEditView.task.text.toString()
                    itemView.desc.text = task.desc
                }
            }

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
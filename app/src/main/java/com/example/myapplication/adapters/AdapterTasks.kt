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
import kotlinx.android.synthetic.main.task_edit_alertdialog.view.*
import kotlinx.android.synthetic.main.task_entry_rv.view.*


// val itemClickedListener: (Task) -> Unit
// Code above takes in a lambda function as a parameter
// Unit == no return type (same as void)

class AdapterTasks(private val group: TaskGroup,
                   private val clickListener: (Task) -> Unit,
                   private val saveFunction: () -> Unit,
                   private val settings: Settings)
    : RecyclerView.Adapter<AdapterTasks.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =  parent.inflate(R.layout.task_entry_rv, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int { return group.taskList.size }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(group.taskList[position], clickListener, saveFunction)
    }

    // ########## ViewHolder ##########
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        // private val selectedIcon = itemView.selected
        private val card = itemView.card
        private val text = itemView.desc

        fun bind(task: Task,
                 taskClicked: (Task) -> Unit,
                 updateSave: () -> Unit) {
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

                // Set current task as hint text and fill in current task, placing cursor at end
                taskEditView.task.hint = itemView.desc.text
                taskEditView.task.setText(itemView.desc.text.toString())
                taskEditView.task.setSelection(itemView.desc.text.toString().length)

                // Cancel button, close dialog
                taskEditView.cancelBtn.setOnClickListener { taskEditDialog.dismiss() }

                // Apply button, make changes if edit made
                taskEditView.applyBtn.setOnClickListener {
                    var updated: Boolean = false
                    val edit:String = taskEditView.task.text.toString()

                    // Check if task edit is new
                    if (edit != task.desc) {
                        updated = true

                        // Apply text change to selected task label and internally
                        task.desc = taskEditView.task.text.toString()
                        itemView.desc.text = task.desc
                    }

                    if (updated) {
                        // Notify main activity to save change made
                        updateSave()
                    }

                    taskEditDialog.dismiss()
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
                taskClicked(task)
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
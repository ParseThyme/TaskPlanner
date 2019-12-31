package com.example.myapplication.adapters

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.*
import com.example.myapplication.data_classes.Task
import com.example.myapplication.data_classes.TaskGroup
import kotlinx.android.synthetic.main.task_edit_alertdialog.view.*
import kotlinx.android.synthetic.main.task_entry_rv.view.*
import java.util.*

// val itemClickedListener: (Task) -> Unit
// Code above takes in a lambda function as a parameter
// Unit == no return type (same as void)

class AdapterTasks(private val group: TaskGroup,
                   private val taskClickedListener: (Task) -> Unit,
                   private val changedDateFn: (Task, String, Int, Int) -> Unit,
                   private val updateSaveFn: () -> Unit,
                   private val settings: Settings)
    : RecyclerView.Adapter<AdapterTasks.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =  parent.inflate(R.layout.task_entry_rv, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int { return group.taskList.size }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(group.taskList[position])
    }

    // ########## ViewHolder ##########
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        // private val selectedIcon = itemView.selected
        private val card = itemView.card
        private val text = itemView.desc

        fun bind(task: Task) {
            // Set description of task when bound
            itemView.desc.text = task.desc

            // Toggle selected icon based on state
            toggleSelected(task.selected)

            // Edit button
            this.itemView.editBtn.setOnClickListener { editTask(task) }

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
                taskClickedListener(task)
            }
        }

        private fun editTask(task: Task) {
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

            // ########## Fill values: ##########
            // 1. Set current task as hint text and fill in current task, placing cursor at end
            taskEditView.task.hint = itemView.desc.text
            taskEditView.task.setText(itemView.desc.text.toString())
            taskEditView.task.setSelection(itemView.desc.text.toString().length)

            // 2. Set date and setup onClick behaviour
            taskEditView.editDateBtn.text = group.date
            var newDate = ""
            var newID = 0
            val cal = Calendar.getInstance()
            val dateSetListener =
                DatePickerDialog.OnDateSetListener { _, year, month, day ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, day)

                    // Change displayed date
                    newDate = dateFormat.format(cal.timeInMillis)
                    newID = idFormat.format(cal.timeInMillis).toInt()
                    taskEditView.editDateBtn.text = newDate
                }
            taskEditView.editDateBtn.setOnClickListener {
                val dialog = DatePickerDialog(taskEditView.context, dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )

                // Assign min + max date then show dialog box
                dialog.datePicker.minDate = minDate
                dialog.datePicker.maxDate = maxDate
                dialog.show()
            }

            // ########## Buttons ##########
            // 1. Cancel: close dialog
            taskEditView.cancelBtn.setOnClickListener { taskEditDialog.dismiss() }

            // 2. Apply: make changes if edit made
            taskEditView.applyBtn.setOnClickListener {
                var updated = false
                val editedText: String = taskEditView.task.text.toString()
                val editedDate: String = taskEditView.editDateBtn.text.toString()

                // 1. Check if task edit is new
                if (editedText != task.desc) {
                    updated = true

                    // Apply text change to selected task label and internally
                    task.desc = taskEditView.task.text.toString()
                    itemView.desc.text = task.desc
                }
                // 2. Check if date has been changed
                if (editedDate != group.date) {
                    updated = true

                    // ToDo: Fix Bug:
                    /* - Group has 2+ tasks
                     * - Top most task selected (highlighted)
                     * - Change date of top most task to new date (new group or existing group doesn't matter)
                     */

                    // Deselect task if selected
                    if (task.selected) {
                        task.selected = !task.selected
                        toggleSelected(task.selected)
                        notifyItemChanged(adapterPosition)
                        group.numSelected--

                        // Call listener function in main activity
                        taskClickedListener(task)
                    }

                    // Notify group adapter to change date
                    changedDateFn(task, newDate, group.id, newID)
                }

                if (updated) {
                    // Notify main activity to save change made
                    updateSaveFn()
                }

                taskEditDialog.dismiss()
            }
        }

        // ########## Toggling functionality ##########
        private fun toggleSelected(isSelected: Boolean) {
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
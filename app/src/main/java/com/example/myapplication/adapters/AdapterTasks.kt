package com.example.myapplication.adapters

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.*
import com.example.myapplication.data_classes.*
import kotlinx.android.synthetic.main.task_edit_alertdialog.view.*
import kotlinx.android.synthetic.main.task_entry_rv.view.*
import java.util.*

// val itemClickedListener: (Task) -> Unit
// Code above takes in a lambda function as a parameter
// Unit == no return type (same as void)

class AdapterTasks(private val group: TaskGroup,
                   private val taskClicked: (Task) -> Unit,
                   private val changedDate: (Task, String, Int, Int) -> Unit,
                   private val updateSave: () -> Unit,
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
        private val taskField = itemView.desc

        fun bind(task: Task) {
            // Set description of task when bound
            taskField.text = task.desc

            // Show/hide tag/time if allocated
            toggleTag(task.tag)
            toggleTime(task.timeStart, task.timeEnd)

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
                if (task.selected)
                    group.numSelected++
                else
                    group.numSelected--

                // Call main click listener function (implemented in main activity)
                taskClicked(task)
            }
        }

        private fun editTask(task: Task) {
            // Set view to be applied to alert dialog
            val taskEditView = LayoutInflater.from(itemView.context).
                inflate(R.layout.task_edit_alertdialog, null)
            // Create builder
            val builder = AlertDialog.Builder(itemView.context).apply {
                setView(taskEditView)
                setCancelable(false)
            }

            // Show dialog
            val taskEditDialog = builder.show()

            // ########## Fill values: ##########
            // 1. Set current task as hint text and fill in previous entry
            taskEditView.btnTag.setImageResourceFromTag(task.tag)
            taskEditView.editedTask.setText(itemView.desc.text.toString())
            taskEditView.editedTask.hint = itemView.desc.text

            // 2. Set date and setup onClick behaviour
            taskEditView.btnEditDate.text = group.date
            var newDate = ""
            var newID = 0
            val cal = Calendar.getInstance()
            val dateSetListener =
                DatePickerDialog.OnDateSetListener { _, year, month, day ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, day)

                    // Change displayed date
                    newDate = createDateLabel(cal)
                    newID = idFormat.format(cal.timeInMillis).toInt()
                    taskEditView.btnEditDate.text = newDate
                }
            taskEditView.btnEditDate.setOnClickListener {
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
            // Cancel: close dialog
            taskEditView.cancelBtn.setOnClickListener { taskEditDialog.dismiss() }

            // Change Tag
            taskEditView.btnTag.setOnClickListener {
                // ToDo
            }

            // Apply: make changes if edit made
            taskEditView.btnApply.setOnClickListener {
                var updated = false
                val editedText: String = taskEditView.editedTask.text.toString()
                val editedDate: String = taskEditView.btnEditDate.text.toString()
                // val editedTag:

                // Check if task edit is new
                if (editedText != task.desc) {
                    updated = true

                    // Apply text change to selected task label and internally
                    task.desc = taskEditView.editedTask.text.toString()
                    itemView.desc.text = task.desc
                }
                // Check if tag has been changed
                /*
                if ()
                */
                // Check if date has been changed
                if (editedDate != group.date) {
                    updated = true

                    // Deselect task if selected
                    if (task.selected) {
                        task.selected = !task.selected
                        toggleSelected(task.selected)
                        group.numSelected--

                        // Call listener function in main activity
                        taskClicked(task)
                    }

                    // Notify group adapter to change date
                    changedDate(task, newDate, group.id, newID)
                }

                // Notify main activity to save change made
                if (updated) { updateSave() }

                taskEditDialog.dismiss()
            }
        }

        private fun toggleSelected(isSelected: Boolean) {
            if (isSelected) { taskField.setBackgroundColor(Color.parseColor(settings.taskHighlightColor)) }
            else { taskField.setBackgroundColor(Color.parseColor(settings.taskBaseColor)) }
        }

        private fun toggleTag(tag: Tag) {
            // No tag, don't display anything
            if (tag == Tag.NONE) {
                itemView.taskTag.visibility = View.GONE
                return
            }

            // Get image, set tag accordingly and display
            itemView.taskTag.setImageResourceFromTag(tag)
            itemView.taskTag.visibility = View.VISIBLE
        }

        private fun toggleTime(timeStart: String, timeEnd: String) {
            // No starting time allocated, meaning no end time has been allocated as well
            if (timeStart == "") {
                itemView.taskTime.visibility = View.GONE
                return
            }

            var timeDisplay: String = ""

            // Set starting date of string
            timeDisplay = timeStart

            // Check if end time allocated (optional value), if so append with spacing in between
            if (timeEnd != "")
                timeDisplay = "$timeStart\n$timeEnd"

            // Set time and show
            itemView.taskTime.text = timeDisplay
            itemView.taskTime.visibility = View.VISIBLE
        }
    }
}
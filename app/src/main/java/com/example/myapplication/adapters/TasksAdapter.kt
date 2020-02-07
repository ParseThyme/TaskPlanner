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
import kotlinx.android.synthetic.main.tag_popup_window.view.*
import kotlinx.android.synthetic.main.task_edit_view.view.*
import kotlinx.android.synthetic.main.task_entry_rv.view.*
import java.util.*

// val itemClickedListener: (Task) -> Unit
// Code above takes in a lambda function as a parameter
// Unit == no return type (same as void)

class TasksAdapter(private val group: TaskGroup,
                   private val taskClicked: (Task) -> Unit,
                   private val changedDate: (Task, String, Int, Int) -> Unit,
                   private val updateSave: () -> Unit,
                   private val settings: Settings)
    : RecyclerView.Adapter<TasksAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =  parent.inflate(R.layout.task_entry_rv, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int { return group.taskList.size }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) { holder.bind(group.taskList[pos]) }

    // ########## ViewHolder ##########
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        private val taskField = itemView.desc

        fun bind(task: Task) {
            // Set description of task when bound
            taskField.text = task.desc

            // Show/hide tag/time if allocated
            toggleTag(task.tag)
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
                if (task.selected) group.numSelected++
                else group.numSelected--

                // Call main click listener function (implemented in main activity)
                taskClicked(task)
            }
        }

        private fun editTask(task: Task) {
            // Set view to be applied to alert dialog
            val view = LayoutInflater.from(itemView.context).
                inflate(R.layout.task_edit_view, null)
            // Create builder
            val builder = AlertDialog.Builder(itemView.context).apply {
                setView(view)
                setCancelable(false)
            }

            // Show dialog
            val taskEditDialog = builder.show()

            // Modifiable values
            var editedTag: Tag = task.tag

            // ########## Fill values: ##########
            // 1. Set current task as hint text and fill in previous entry
            view.btnTag.setImageResourceFromTag(task.tag)
            view.txtEditTaskDesc.setText(itemView.desc.text.toString())
            view.txtEditTaskDesc.hint = itemView.desc.text

            // 2. Set date and setup onClick behaviour
            view.btnEditDate.text = group.date
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
                    view.btnEditDate.text = newDate
                }
            view.btnEditDate.setOnClickListener {
                val dialog = DatePickerDialog(view.context, dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )

                // Assign min + max date then show dialog box
                dialog.datePicker.minDate = minDate
                dialog.datePicker.maxDate = maxDate
                dialog.show()
            }

            // ########## Listeners ##########
            // Close keyboard when editText loses focus
            view.txtEditTaskDesc.closeKeyboardOnFocusLost()

            // Change Tag
            view.btnTag.setOnClickListener {
                val window = itemView.context.createTagPopupWindow(view.btnTag)
                window.contentView.tagGroup.setOnCheckedChangeListener { _, chosenTag ->
                    when (chosenTag) {
                        R.id.tagNone -> editedTag = Tag.NONE
                        R.id.tagEvent -> editedTag = Tag.EVENT
                        R.id.tagBooking -> editedTag = Tag.BOOKING
                        R.id.tagBuy -> editedTag = Tag.BUY
                    }

                    view.btnTag.setImageResourceFromTag(editedTag)
                    window.dismiss()
                }
            }

            // Cancel: close dialog
            view.cancelBtn.setOnClickListener { taskEditDialog.dismiss() }

            // Apply: make changes if edit made
            view.btnApply.setOnClickListener {
                var updated = false
                val editedText: String = view.txtEditTaskDesc.text.toString()
                val editedDate: String = view.btnEditDate.text.toString()

                // Check if task edit is new
                if (editedText != task.desc && editedText != "") {
                    updated = true

                    // Apply text change to display and internal value
                    task.desc = view.txtEditTaskDesc.text.toString()
                    itemView.desc.text = task.desc
                }
                // Check if tag has been changed
                if (editedTag != task.tag) {
                    updated = true

                    // Apply change to display and internal value
                    task.tag = editedTag
                    toggleTag(task.tag)
                    //itemView.taskTag.setImageResourceFromTag(task.tag)
                }

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
            if (tag == Tag.NONE) { itemView.taskTag.visibility = View.GONE }
            // Get image, set tag accordingly and display
            else {
                itemView.taskTag.setImageResourceFromTag(tag)
                itemView.taskTag.visibility = View.VISIBLE
            }
        }

        private fun toggleTime(time: TaskTime) {
            // Generate time to display
            val timeDisplay = time.createDisplayedTime()

            // Time unallocated, hide display
            if (!time.isValid()) {
                itemView.taskTime.visibility = View.GONE
                return
            }

            // Otherwise set time and show
            itemView.taskTime.text = timeDisplay
            itemView.taskTime.visibility = View.VISIBLE
        }
    }
}
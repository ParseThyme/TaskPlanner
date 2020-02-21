package com.example.myapplication.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.*
import com.example.myapplication.data_classes.*
import com.example.myapplication.popup_windows.PopupDate
import com.example.myapplication.popup_windows.PopupTag
import kotlinx.android.synthetic.main.task_edit_view.view.*
import kotlinx.android.synthetic.main.task_entry_rv.view.*


// val itemClickedListener: (Task) -> Unit
// Code above takes in a lambda function as a parameter
// Unit == no return type (same as void)

class TasksAdapter(private val group: TaskGroup,
                   private val taskClicked: (Task) -> Unit,
                   private val changedDate: (Task, TaskDate, Int) -> Unit,
                   private val updateSave: () -> Unit,
                   private val settings: Settings)
    : RecyclerView.Adapter<TasksAdapter.ViewHolder>()
{
    lateinit var datePopup: PopupDate
    lateinit var tagPopup: PopupTag

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =  parent.inflate(R.layout.task_entry_rv, false)
        return ViewHolder(v)
    }
    override fun getItemCount(): Int { return group.taskList.size }
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) { holder.bind(group.taskList[pos]) }

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
            val view: View = LayoutInflater.from(itemView.context).inflate(R.layout.task_edit_view, null)
            // Create builder
            val builder = AlertDialog.Builder(itemView.context).apply {
                setView(view)
                setCancelable(false)
            }

            // Show dialog
            val dialog: AlertDialog = builder.show()

            // Popups
            datePopup = PopupDate(view.btnEditDate, settings, view.context)
            tagPopup = PopupTag(view.btnSetTag, view.context)

            // ########## Fill values: ##########
            // 1. Set current task as hint text and fill in previous entry
            view.btnSetTag.setImageResourceFromTag(task.tag)
            view.txtEditTaskDesc.setText(itemView.desc.text.toString())
            view.txtEditTaskDesc.hint = itemView.desc.text

            // 2. Set date and setup onClick behaviour
            view.btnEditDate.text = group.date.createLabel(Label.Abbreviated)
            view.btnEditDate.setOnClickListener { datePopup.create() }

            // ########## Listeners ##########
            // Close keyboard when editText loses focus
            view.txtEditTaskDesc.closeKeyboardOnFocusLost()

            // Change Tag
            view.btnSetTag.setOnClickListener { tagPopup.create() }

            // Cancel: close dialog
            view.cancelBtn.setOnClickListener { dialog.dismiss() }

            // Apply: make changes if edit made
            view.btnApply.setOnClickListener {
                var updated = false
                val editedText: String = view.txtEditTaskDesc.text.toString()
                val editedDate: String = view.btnEditDate.text.toString()
                val editedTag: Tag = view.btnSetTag.getTagFromImageResource()

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
                }

                // Check if date has been changed
                if (editedDate != group.date.createLabel()) {
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
                    changedDate(task, datePopup.setDate, group.date.id)
                }

                // Notify main activity to save change made
                if (updated) { updateSave() }

                dialog.dismiss()
            }
        }

        private fun toggleSelected(isSelected: Boolean) {
            if (isSelected) { taskField.applyBackgroundColor(settings.highlightColor) }
            else { taskField.applyBackgroundColor(settings.taskBaseColor) }
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
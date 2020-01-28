package com.example.myapplication.adapters

import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.Settings
import com.example.myapplication.data_classes.*
import com.example.myapplication.inflate
import kotlinx.android.synthetic.main.task_group_rv.view.*

class TaskGroupAdapter(private val taskGroupList: ArrayList<TaskGroup>,
                       private val settings: Settings,
                       private val taskClicked: (Task) -> Unit,
                       private val dateClicked: (Int) -> Unit,
                       private val scrollTo: (Int) -> Unit,
                       private val updateSave: () -> Unit)
    : RecyclerView.Adapter<TaskGroupAdapter.ViewHolder>() {

    // Date changed for task
    private val changedDate = {
        // Params:
            task: Task, newDate: String, oldID: Int, newID: Int
        // Function to call:
        -> changeGroup(task, newDate, oldID, newID)
    }

    // Total task count (from entire recycler view). Public get, private set
    var taskCount: Int = 0
        private set

    // Used for sorting, default value ensures new min value is always replaced with first entry
    private val baseMinDate: Int = 90000000
    private var minDate: Int = 0

    init {
        // Updating values based on previously saved list
        if (taskGroupList.size > 0) {
            for (group in taskGroupList)
                taskCount += group.taskList.size

            minDate = taskGroupList[0].id
        }
        else {
            Log.d("Test", "Empty")
            minDate = baseMinDate
        }
    }

    // Number of items in table view
    override fun getItemCount(): Int { return taskGroupList.size }

    // Creating cell (date group entry)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Use inflate function found in Util then return containing cell layout and clickListener
        val inflatedView = parent.inflate(R.layout.task_group_rv, false)
        return ViewHolder(inflatedView)
    }

    // When cell made
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Apply updates to UI based on internal values
        holder.bind(taskGroupList[position])
    }

    // ########## Group related functionality ##########
    fun addTask(id: Int, date: String, desc: String, tag: Tag = Tag.NONE) {
        taskCount++

        // ---------- Auto Sorting Entries ----------
        // [A]. Check for earliest date
        if (id < minDate) {
            // New date is earlier, make it the new min date and insert new group at the top
            minDate = id
            addNewTaskGroup(0, date, Task(desc, tag), id)
            return
        }

        // [B]. Otherwise start from latest entry and move upwards
        for (pos in taskGroupList.lastIndex downTo 0 step 1) {
            // [1]. Matching date, append to existing list of tasks
            if (id == taskGroupList[pos].id) {
                addToTaskGroup(pos, Task(desc, tag))
                return
            }
            // [2]. Date reached is earlier, create new date category with new task
            if (id > taskGroupList[pos].id) {
                addNewTaskGroup(pos + 1, date, Task(desc, tag), id)
                return
            }
        }
    }

    private fun addNewTaskGroup(pos: Int, date: String, newTask: Task, id: Int) {
        taskGroupList.add(pos, TaskGroup(date, arrayListOf(newTask), id))
        notifyItemInserted(pos)
    }

    private fun addToTaskGroup(pos: Int, newTask: Task) {
        taskGroupList[pos].taskList.add(newTask)
        notifyItemChanged(pos)
    }

    // ########## Modifying task entries ##########
    fun deleteSelected(selected : Int) {
        // [1]. Clearing entire list
        if (selected == taskCount) {
            // Empty everything and reset values
            taskGroupList.clear()
            notifyDataSetChanged()
            taskCount = 0
            minDate = baseMinDate
            return
        }

        // [2]. Deleting specifically selected tasks
        var count = 0
        val end = taskGroupList.size - 1

        for (groupNum in end downTo 0) {
            val group = taskGroupList[groupNum]
            if (group.numSelected > 0) {
                // Go through task list in group, deleting selected tasks
                for (taskNum in group.taskList.size - 1 downTo 0) {
                    val currTask = group.taskList[taskNum]
                    if (currTask.selected) {
                        group.taskList.remove(currTask)
                        notifyItemChanged(groupNum)

                        // Update counters, number selected in current group and overall counter
                        group.numSelected--
                        taskCount--
                        count++

                        // Delete entire group itself once the task count reaches 0
                        if (group.taskList.size == 0) {
                            // Update minDate if removed date was the minimum one
                            if (group.id == minDate)
                                minDate = taskGroupList[groupNum + 1].id

                            taskGroupList.removeAt(groupNum)
                            notifyItemRemoved(groupNum)
                        }

                        // Exit entire function if all selected tasks have been removed
                        if (count == selected)
                            return

                        // Exit early once numSelected = 0 (no more selected tasks in this group)
                        if (group.numSelected == 0)
                            break
                    }
                }
            }
        }
    }

    fun toggleGroupSelected(groupNum : Int) : Int {
        var difference = 0
        val group: TaskGroup = taskGroupList[groupNum]

        // A. Deselect all in group
        if (group.allSelected()) {
            for (i in 0 until group.taskList.size)
                group.taskList[i].selected = false

            group.numSelected = 0
            difference = -(group.taskList.size)
        }
        // B. Select all in group
        else {
            for (i in 0 until group.taskList.size) {
                val task: Task = group.taskList[i]

                // Task not already selected, highlight and select it
                if (!task.selected) {
                    difference += 1
                    group.numSelected += 1
                    group.taskList[i].selected = true

                    if (group.numSelected == group.taskList.size) { break }
                }
            }
        }

        notifyItemChanged(groupNum)
        return difference
    }

    fun toggleAll(selectAll : Boolean = true) {
        val end: Int = taskGroupList.size - 1
        for (groupNum in end downTo 0) {
            val group = taskGroupList[groupNum]

            // Go through each individual task and select/deselect it
            for (taskNum in group.taskList.size - 1 downTo 0) {
                group.taskList[taskNum].selected = selectAll
            }

            // Depending on overall call, either set num selected to group count or 0. Also modify collapse/expand state
            if (selectAll)
                group.numSelected = group.taskList.size
            else
                group.numSelected = 0

            notifyItemChanged(groupNum)
        }
    }

    private fun changeGroup(task: Task, newDate: String, oldID: Int, newID: Int) {
        // "Move" to new position (add new task). -- to balance out addition made in addTask()
        addTask(newID, newDate, task.desc, task.tag)
        taskCount--

        // Find old group and remove it at the old position
        for (index in 0 until taskGroupList.size) {
            val group = taskGroupList[index]
            if (group.id == oldID) {
                group.taskList.remove(task)

                // Remove group if its list is exhausted
                if (group.taskList.size == 0) {
                    // Update minDate if removed date was the minimum one
                    if (group.id == minDate)
                        minDate = taskGroupList[index + 1].id

                    taskGroupList.removeAt(index)
                    notifyItemRemoved(index)
                }
                else { notifyItemChanged(index) }
                break
            }
        }
    }

    // ########## ViewHolder ##########
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // Defining reference to task description text in layout
        private val tasksRV = itemView.taskGroupRV
        private val dateLabel = itemView.dateLabel

        fun bind(group: TaskGroup) {
            // Assign date label
            dateLabel.text = group.date

            // When date label clicked, call ActivityMain click listener function
            itemView.dateCard.setOnClickListener { if (group.isExpanded()) dateClicked(adapterPosition) }

            // Load previously saved collapse/expand states
            toggleExpandCollapseState(group.state, group)

            // Closing/Opening group tab
            itemView.collapseExpandBtn.setOnClickListener {
                val newState: ViewState = group.toggleExpandCollapse()
                toggleExpandCollapseState(newState, group)

                // Scroll position, ensure entire group + contents visible when expanded
                if (newState == ViewState.EXPANDED) { scrollTo(adapterPosition) }
            }

            // Store reference to task adapter
            val taskAdapter = TasksAdapter(group, taskClicked, changedDate, updateSave, settings)
            // Assign layout manager + adapter
            tasksRV.apply {
                layoutManager = LinearLayoutManager(tasksRV.context, RecyclerView.VERTICAL, false)
                adapter = taskAdapter
            }
        }

        private fun toggleExpandCollapseState(state: ViewState, group: TaskGroup) {
            if (state == ViewState.EXPANDED) {
                // Change background color back to normal (if modified)
                itemView.collapseExpandBtn.setBackgroundColor(Color.TRANSPARENT)

                // Open group and update icon (only do so if not done already)
                itemView.taskGroupRV.visibility = View.VISIBLE
                itemView.collapseExpandBtn.setImageResource(R.drawable.ic_arrow_filled_down)
            } else {
                // If a task has been selected, highlight background to indicate
                if (group.numSelected != 0)
                    itemView.collapseExpandBtn.setBackgroundColor(Color.parseColor(settings.taskHighlightColor))
                else // Clear highlights (via selectAll toggle when collapsed)
                    itemView.collapseExpandBtn.setBackgroundColor(Color.TRANSPARENT)

                // Close group, and change icon
                itemView.taskGroupRV.visibility = View.GONE
                itemView.collapseExpandBtn.setImageResource(R.drawable.ic_arrow_filled_up)
            }

            // Save change to view state
            updateSave()
        }
    }
}
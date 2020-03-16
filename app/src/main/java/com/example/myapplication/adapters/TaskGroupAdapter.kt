package com.example.myapplication.adapters

import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.*
import com.example.myapplication.data_classes.*
import com.example.myapplication.utility.Settings
import com.example.myapplication.utility.applyBackgroundColor
import com.example.myapplication.utility.printDebugMsg
import com.example.myapplication.utility.inflate
import kotlinx.android.synthetic.main.task_group_rv.view.*

class TaskGroupAdapter(private val data: TaskListData,
                       private val taskGroupList: ArrayList<TaskGroup>,
                       private val taskClicked: (Task) -> Unit,
                       private val dateClicked: (Int) -> Unit,
                       private val scrollTo: (Int) -> Unit,
                       private val changeCollapseExpandIcon: (Fold) -> Unit,
                       private val updateSave: () -> Unit)
    : RecyclerView.Adapter<TaskGroupAdapter.ViewHolder>() {
    // Listener function: Date changed for task
    private val changeDate = {
        // Params:
            task: Task, newDate: TaskDate, oldID: Int
        // Function to call:
        -> changeGroup(task, newDate, oldID)
    }

    // Used for sorting, default value ensures new min value is always replaced with first entry
    private val baseMinDate: Int = 90000000
    private var minDate: Int = baseMinDate

    // Initialization
    init {
        // Remove older entries, assuming previous save exists
        if (Settings.deleteOldDates) { deleteOldTasks() }

        // Given save exists, update values based on previously saved list
        if (taskGroupList.isNotEmpty()) {
            // Go through each group for taskCount and numCollapsed. Clear existing selections
            for (group: TaskGroup in taskGroupList) {
                data.taskCount += group.taskList.size

                // Clear previous selections and update collapse count if group is collapsed
                group.setSelected(false)
                if (!group.isFoldedOut())
                    data.numFoldedIn++
            }

            // Set minimum date to first entry and check if expand collapse icon needs updating
            minDate = taskGroupList[0].date.id
            updateExpandCollapseIcon()
        }
    }

    // Number of items in table view
    override fun getItemCount(): Int { return taskGroupList.size }
    // When group made, apply updates to UI based on internal values
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) { holder.bind(taskGroupList[pos]) }
    // Creating cell (date group entry)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Use inflate function found in Util then return containing cell layout and clickListener
        val inflatedView = parent.inflate(R.layout.task_group_rv, false)
        return ViewHolder(inflatedView)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // Defining reference to task description text in layout
        private val tasksRV = itemView.rvTaskGroup
        private val dateLabel = itemView.labelDate
        private val dayLabel = itemView.labelDay
        private val foldBtn = itemView.btnFold

        fun bind(group: TaskGroup) {
            // Assign date label
            dateLabel.text = group.date.createLabel()
            dayLabel.text = group.date.getDayNameShort()

            // Update view if collapsed/expanded
            setFold(group)

            // When day label clicked, call ActivityMain click listener function (De/Select entire group)
            dayLabel.setOnClickListener { if (group.isFoldedOut()) dateClicked(adapterPosition) }

            // Update view if collapsed/expanded (Clicked)
            foldBtn.setOnClickListener {
                val newState: Fold = group.toggleFold()
                setFold(group)

                // Update collapsed counts, scroll position if expanded, ensure entire group visible
                when (newState) {
                    Fold.OUT -> {
                        scrollTo(adapterPosition)
                        data.numFoldedIn--
                    }
                    Fold.IN ->
                        data.numFoldedIn++
                }
                updateExpandCollapseIcon()

                // Save change to view state
                updateSave()
            }

            // Assign layout manager + adapter
            tasksRV.apply {
                layoutManager = LinearLayoutManager(tasksRV.context, RecyclerView.VERTICAL, false)
                adapter = TasksAdapter(group, taskClicked, changeDate, updateSave)
            }
        }

        private fun setFold(group: TaskGroup) {
            // Only apply if change is being made (e.g. Expand on already expanded date should do nothing)
            if (group.state.isNew(itemView.rvTaskGroup)) {
                when (group.state) {
                    Fold.OUT -> {
                        // Change background color back to normal (if modified)
                        itemView.btnFold.setBackgroundColor(Color.TRANSPARENT)

                        // Open group and update icon (only do so if not done already)
                        itemView.rvTaskGroup.visibility = View.VISIBLE
                        itemView.btnFold.setImageResource(R.drawable.ic_arrow_filled_down)
                    }

                    Fold.IN -> {
                        // Close group, and change icon
                        itemView.rvTaskGroup.visibility = View.GONE
                        itemView.btnFold.setImageResource(R.drawable.ic_arrow_filled_up)
                    }
                }
            }

            // Toggling arrow icon highlighting when group collapsed
            if (group.state == Fold.IN) {
                // If a task has been selected, highlight background to indicate
                if (group.numSelected != 0)
                    itemView.btnFold.applyBackgroundColor(Settings.highlightColor)
                else // Clear highlights (via selectAll toggle when collapsed)
                    itemView.btnFold.applyBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    // ##############################
    // Additional Functions
    // ##############################

    // ########## Getters/Setters ##########
    fun allCollapsed() : Boolean { return data.numFoldedIn == taskGroupList.size }

    // ########## Group related functionality ##########
    fun addTask(date: TaskDate, task: Task) {
        data.taskCount++

        // ---------- Auto Sorting Entries ----------
        // [A]. Check for earliest date
        if (date.id < minDate) {
            // New date is earlier, make it the new min date and insert new group at the top
            minDate = date.id
            addNewTaskGroup(0, date, task)
            return
        }

        // [B]. Otherwise start from latest entry and move upwards
        for (pos in taskGroupList.lastIndex downTo 0 step 1) {
            // [1]. Matching date, append to existing list of tasks
            if (date.id == taskGroupList[pos].date.id) {
                addToTaskGroup(pos, task)
                return
            }
            // [2]. Date reached is earlier, create new date category with new task
            if (date.id > taskGroupList[pos].date.id) {
                addNewTaskGroup(pos + 1, date, task)
                return
            }
        }
    }

    private fun addNewTaskGroup(pos: Int, date: TaskDate, newTask: Task) {
        taskGroupList.add(pos, TaskGroup(date, arrayListOf(newTask)))
        notifyItemInserted(pos)

        // Update collapse/expand icon to enable collapsing as new entry will always be expanded
        changeCollapseExpandIcon(Fold.OUT)
    }

    private fun addToTaskGroup(pos: Int, newTask: Task) {
        taskGroupList[pos].taskList.add(newTask)
        notifyItemChanged(pos)
    }

    // ########## Modifying selected entries ##########
    fun delete() {
        // Delete all selected
        if (data.allSelected()) {
            // Empty everything and reset values
            taskGroupList.clear()
            data.deleteSelected()
            minDate = baseMinDate
            notifyDataSetChanged()
            return
        }

        // Otherwise go through groups
        var groupDeleted = false
        for (groupNum: Int in taskGroupList.size - 1 downTo 0) {
            val group: TaskGroup = taskGroupList[groupNum]
            // If group has children selected, perform deletion
            if (group.numSelected != 0) {
                group.selectedDelete(data)
                notifyItemChanged(groupNum)

                // Check if we need to delete group itself (when number of children == 0)
                if (group.isEmpty()) {
                    // Update minDate if removed date was the minimum one
                    if (group.date.id == minDate)
                        minDate = taskGroupList[groupNum + 1].date.id

                    taskGroupList.removeAt(groupNum)
                    notifyItemRemoved(groupNum)
                    groupDeleted = true
                }

                // Once all selected tasks deleted, exit early
                if (data.numSelected == 0) break
            }
        }

        // Check to see if collapse/expand all icon needs to be updated (from group deletion)
        if(groupDeleted) { updateExpandCollapseIcon() }
        notifyDataSetChanged()
    }

    fun clearSelected(paramType: TaskParam) {
        // Uses same logic as delete(). We don't track group size in this case.
        for (groupNum: Int in taskGroupList.size - 1 downTo 0) {
            val group: TaskGroup = taskGroupList[groupNum]
            if (group.numSelected != 0) {
                group.selectedClear(data, paramType)
                notifyItemChanged(groupNum)
                if (data.numSelected == 0) break
            }
        }
    }

    fun setTagForSelected(newTag: Int) {
        for (groupNum: Int in taskGroupList.size - 1 downTo 0) {
            val group: TaskGroup = taskGroupList[groupNum]
            if (group.numSelected != 0) {
                group.selectedSetTag(data, newTag)
                notifyItemChanged(groupNum)
                if (data.numSelected == 0) break
            }
        }
    }

    fun changeDateForSelected(newDate: TaskDate) {
        // Store list of tasks to be changed
        val movedTasks: ArrayList<Task> = arrayListOf()

        // Case 1: All selected, move everything
        if (data.allSelected()) {
            // Copy over every task
            for (group in taskGroupList) { movedTasks.addAll(group.taskList) }

            // Clear data, set new date as minimum date (as it will be the only one)
            data.numSelected = 0
            taskGroupList.clear()
            minDate = newDate.id
            notifyDataSetChanged()

            // Create the new group and add all tasks to it
            movedTasks[0].selected = false                       // Clear selection from first task
            addNewTaskGroup(0, newDate, movedTasks[0])
            for (taskNum in 1 until movedTasks.size) {
                movedTasks[taskNum].selected = false
                addToTaskGroup(0, movedTasks[taskNum])      // Clear selections from tasks
                notifyItemInserted(0)
            }
        }
        // Case 2: Not everything selected
        else {
            // 1. Go through each group
            for (groupNum: Int in taskGroupList.size - 1 downTo 0) {
                val group: TaskGroup = taskGroupList[groupNum]

                // 2. Check if group has any tasks selected
                // A. All group's tasks selected, copy over taskList then delete group
                if (group.allSelected()) {
                    // If group was min date (first entry), set min date to next group
                    if (group.date.id == minDate)
                        minDate = taskGroupList[groupNum + 1].date.id

                    movedTasks.addAll(group.taskList)
                    taskGroupList.remove(group)
                    data.taskCount -= data.numSelected
                    data.numSelected -= group.taskList.count()
                    notifyItemChanged(groupNum)

                    if (data.numSelected == 0) break
                }
                // B. Between 1 - group size selected, copy only selected tasks and delete them
                else if (group.numSelected >= 0) {
                    for (taskNum: Int in group.taskList.size - 1 downTo 0) {
                        if (group.taskList[taskNum].selected) {
                            movedTasks.add(group.taskList[taskNum])
                            group.taskList.removeAt(taskNum)
                            group.numSelected--
                            data.numSelected--
                            data.taskCount--
                        }
                    }
                    notifyItemChanged(groupNum)
                    if (data.numSelected == 0) break
                }
            }

            // 2. For all moved tasks, add them to their new group
            for (task in movedTasks) {
                task.selected = false   // Deselect any selected tasks
                addTask(newDate, task)  // Add them to their new group
            }
        }
    }

    // ########## Toggling ##########
    fun toggleGroupSelected(groupNum : Int) : Int {
        val group: TaskGroup = taskGroupList[groupNum]
        val numSelectedPreToggle = group.numSelected

        group.toggleSelected()
        val difference = group.numSelected - numSelectedPreToggle

        notifyItemChanged(groupNum)
        return difference
    }

    fun toggleSelectAll(selectAll : Boolean = true) {
        val end: Int = taskGroupList.size - 1
        for (groupNum in end downTo 0)
            taskGroupList[groupNum].setSelected(selectAll)

        notifyDataSetChanged()
    }

    fun toggleFoldAll(newState: Fold = Fold.OUT) {
        val end: Int = taskGroupList.size - 1
        for (groupNum in end downTo 0)
            taskGroupList[groupNum].state = newState
        notifyDataSetChanged()

        // Update collapsed count, 0 when all groups expanded, and maximum count when all collapsed
        data.numFoldedIn = when (newState) {
            Fold.OUT -> 0
            Fold.IN -> taskGroupList.size
        }
    }

    /**
     * Change group task belongs to
     * @param task: Task being updated
     * @param newDate: New group task is switching to
     * @param oldID: Old group task belonged to
     */
    private fun changeGroup(task: Task, newDate: TaskDate, oldID: Int) {
        // "Move" to new position (add new task). -- to balance out addition made in addTask()
        addTask(newDate, task)
        data.taskCount--

        // Find old group and remove it at the old position
        for (index in 0 until taskGroupList.size) {
            val group = taskGroupList[index]
            if (group.date.id == oldID) {
                group.taskList.remove(task)

                // Remove group if its list is exhausted
                if (group.taskList.size == 0) {
                    // Update minDate if removed date was the minimum one
                    if (group.date.id == minDate)
                        minDate = taskGroupList[index + 1].date.id

                    taskGroupList.removeAt(index)
                    notifyItemRemoved(index)
                }
                else { notifyItemChanged(index) }
                break
            }
        }
    }

    private fun updateExpandCollapseIcon() {
        // Update icon accordingly based on number collapsed
        when (data.numFoldedIn) {
            taskGroupList.size - 1 -> changeCollapseExpandIcon(Fold.OUT)  // Expandable
            taskGroupList.size -> changeCollapseExpandIcon(Fold.IN)     // All collapsed
        }
    }

    // ########## Other ##########
    private fun deleteOldTasks() {
        val iterator: MutableListIterator<TaskGroup> = taskGroupList.listIterator()
        while (iterator.hasNext()) {
            val group: TaskGroup = iterator.next()
            if (group.date.isPastDate())
                iterator.remove()
            else
                break   // Stop when date is today's date or later
        }
    }
}
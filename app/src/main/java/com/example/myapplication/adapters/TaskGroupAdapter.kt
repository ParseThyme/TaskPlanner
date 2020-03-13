package com.example.myapplication.adapters

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.*
import com.example.myapplication.data_classes.*
import kotlinx.android.synthetic.main.task_group_rv.view.*

class TaskGroupAdapter(private val data: TaskListData,
                       private val taskGroupList: ArrayList<TaskGroup>,
                       private val taskClicked: (Task) -> Unit,
                       private val dateClicked: (Int) -> Unit,
                       private val scrollTo: (Int) -> Unit,
                       private val changeCollapseExpandIcon: (ViewState) -> Unit,
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
        // Previous save exists, update values based on previously saved list
        if (taskGroupList.size > 0) {
            // Go through each group for taskCount and numCollapsed. Clear existing selections
            for (group in taskGroupList) {
                data.taskCount += group.taskList.size

                // Clear previous selections and update collapse count if group is collapsed
                group.setHighlight(false)
                if (!group.isExpanded())
                    data.numCollapsed++
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
        private val tasksRV = itemView.taskGroupRV
        private val dateLabel = itemView.dateLabel
        private val dayLabel = itemView.dayLabel
        private val collapseExpandBtn = itemView.collapseExpandBtn

        fun bind(group: TaskGroup) {
            // Assign date label
            dateLabel.text = group.date.createLabel()
            dayLabel.text = group.date.getDayNameShort()

            // Update view if collapsed/expanded
            setExpandCollapse(group)

            // When day label clicked, call ActivityMain click listener function (De/Select entire group)
            dayLabel.setOnClickListener { if (group.isExpanded()) dateClicked(adapterPosition) }

            // Update view if collapsed/expanded (Clicked)
            collapseExpandBtn.setOnClickListener {
                val newState: ViewState = group.toggleExpandCollapse()
                setExpandCollapse(group)

                // Update collapsed counts, scroll position if expanded, ensure entire group visible
                when (newState) {
                    ViewState.EXPANDED -> {
                        scrollTo(adapterPosition)
                        data.numCollapsed--
                    }
                    ViewState.COLLAPSED ->
                        data.numCollapsed++
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

        private fun setExpandCollapse(group: TaskGroup) {
            // Only apply if change is being made (e.g. Expand on already expanded date should do nothing)
            if (group.state.isNewState(itemView.taskGroupRV)) {
                when (group.state) {
                    ViewState.EXPANDED -> {
                        // Change background color back to normal (if modified)
                        itemView.collapseExpandBtn.setBackgroundColor(Color.TRANSPARENT)

                        // Open group and update icon (only do so if not done already)
                        itemView.taskGroupRV.visibility = View.VISIBLE
                        itemView.collapseExpandBtn.setImageResource(R.drawable.ic_arrow_filled_down)
                    }

                    ViewState.COLLAPSED -> {
                        // Close group, and change icon
                        itemView.taskGroupRV.visibility = View.GONE
                        itemView.collapseExpandBtn.setImageResource(R.drawable.ic_arrow_filled_up)
                    }
                }
            }

            // Toggling arrow icon highlighting when group collapsed
            if (group.state == ViewState.COLLAPSED) {
                // If a task has been selected, highlight background to indicate
                if (group.numSelected != 0)
                    itemView.collapseExpandBtn.applyBackgroundColor(Settings.highlightColor)
                else // Clear highlights (via selectAll toggle when collapsed)
                    itemView.collapseExpandBtn.applyBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    // ##############################
    // Additional Functions
    // ##############################

    // ########## Getters/Setters ##########
    fun allCollapsed() : Boolean { return data.numCollapsed == taskGroupList.size }

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
        changeCollapseExpandIcon(ViewState.EXPANDED)
    }

    private fun addToTaskGroup(pos: Int, newTask: Task) {
        taskGroupList[pos].taskList.add(newTask)
        notifyItemChanged(pos)
    }

    // ########## Modifying task entries ##########
    fun deleteSelected() {
        var groupDeleted = false

        // [1]. Clearing entire list
        if (data.allSelected()) {
            // Empty everything and reset values
            taskGroupList.clear()
            notifyDataSetChanged()
            data.deleteSelected()
            minDate = baseMinDate
            return
        }

        // [2]. Deleting specifically selected tasks
        var count = 0

        // Go through each group and delete any selected tasks
        main_loop@for (groupNum in taskGroupList.size - 1 downTo 0) {
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
                        // data.taskCount--
                        count++

                        // Delete entire group itself once the task count reaches 0
                        if (group.taskList.size == 0) {
                            // Update minDate if removed date was the minimum one
                            if (group.date.id == minDate)
                                minDate = taskGroupList[groupNum + 1].date.id

                            taskGroupList.removeAt(groupNum)
                            notifyItemRemoved(groupNum)
                            groupDeleted = true
                        }

                        // Exit entire loop if all selected tasks have been removed
                        if (count == data.numSelected)
                            break@main_loop

                        // Exit inner loop early once numSelected = 0 (no more selected tasks in this group)
                        if (group.numSelected == 0)
                            break
                    }
                }
            }
        }

        // Check to see if collapse/expand all icon needs to be updated
        if(groupDeleted) { updateExpandCollapseIcon() }
        data.deleteSelected()
    }

    fun toggleGroupSelected(groupNum : Int) : Int {
        val group: TaskGroup = taskGroupList[groupNum]
        val numSelectedPreToggle = group.numSelected

        group.toggleHighlight()
        val difference = group.numSelected - numSelectedPreToggle

        notifyItemChanged(groupNum)
        return difference
    }

    fun toggleAllHighlight(selectAll : Boolean = true) {
        val end: Int = taskGroupList.size - 1
        for (groupNum in end downTo 0)
            taskGroupList[groupNum].setHighlight(selectAll)

        notifyDataSetChanged()
    }

    fun toggleAllExpandCollapse(newState: ViewState = ViewState.EXPANDED) {
        val end: Int = taskGroupList.size - 1
        for (groupNum in end downTo 0)
            taskGroupList[groupNum].state = newState

        notifyDataSetChanged()

        // Update collapsed count, 0 when all groups expanded, and maximum count when all collapsed
        data.numCollapsed = when (newState) {
            ViewState.EXPANDED -> 0
            ViewState.COLLAPSED -> taskGroupList.size
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
        when (data.numCollapsed) {
            taskGroupList.size - 1 -> changeCollapseExpandIcon(ViewState.EXPANDED)  // Expandable
            taskGroupList.size -> changeCollapseExpandIcon(ViewState.COLLAPSED)     // All collapsed
        }
    }
}
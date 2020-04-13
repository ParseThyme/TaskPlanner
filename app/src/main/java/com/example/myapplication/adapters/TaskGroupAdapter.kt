package com.example.myapplication.adapters

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.*
import com.example.myapplication.data_classes.*
import com.example.myapplication.utility.*
import kotlinx.android.synthetic.main.task_group_header.view.*
import kotlinx.android.synthetic.main.task_group_rv.view.*
import kotlin.collections.ArrayList

class TaskGroupAdapter(private val taskGroupList: ArrayList<TaskGroup>,
                       // private val taskClicked: (Task) -> Unit,
                       private val taskClicked: (SelectedTask) -> Unit,
                       private val dateClicked: (Int) -> Unit,
                       private val scrollTo: (Int) -> Unit,
                       private val changeCollapseExpandIcon: (Fold) -> Unit,
                       private val updateSave: () -> Unit)
    : RecyclerView.Adapter<TaskGroupAdapter.ViewHolder>() {

    // Used for sorting, default value ensures new min value is always replaced with first entry
    private val baseMinDate: Int = 90000000
    private var minDate: Int = baseMinDate

    // Assigned headers
    private val headers = hashMapOf (
        // Key: [Period], Value = [Boolean]
        Period.PAST      to false,
        Period.THIS_WEEK to false,
        Period.NEXT_WEEK to false,
        Period.FORTNIGHT to false,
        Period.FUTURE    to false
    )
    private var headersAssigned = 0

    // Initialization
    init {
        // Remove older entries, requires setting toggled on
        if (Settings.deleteOldDates) { deleteOldTasks() }

        // If previous data exists, set min date to first entry + check if expand collapse icon needs updating
        if (taskGroupList.isNotEmpty()) {
            minDate = taskGroupList[0].date.id
            updateExpandCollapseIcon()
        }

        // Setup tracker
        Tracker.init(taskGroupList)
    }

    // ##############################
    // Headers
    // ##############################
    private fun TaskGroup.assignHeader() : Boolean {
        // If all headers have been assigned, exit
        if (headersAssigned == headers.size) return false

        // 1. Get period group belongs to
        val period: Period = date.getPeriod()
        // 2. Check if period assigned yet, if not mark to assign, update counter
        if (headers[period] == false) {
            headers[period] = true
            headersAssigned++
            return true
        }

        return false
    }

    // Number of items in table view
    override fun getItemCount(): Int { return taskGroupList.size }
    // When group made, apply updates to UI based on internal values
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        holder.bind(taskGroupList[pos])
    }
    // Creating cell (date group entry)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Determine view to inflate: header or standard row
        val inflatedView = when (viewType) {
            GroupType.GROUP.ordinal -> parent.inflate(R.layout.task_group_rv)   // Group
            else -> parent.inflate(R.layout.task_group_header)                  // Header
        }
        return ViewHolder(inflatedView)
    }
    override fun getItemViewType(position: Int): Int {
        // 0 = Header, 1 = Standard Group
        return taskGroupList[position].groupType.ordinal
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // Bind content based on view passed in
        fun bind(group: TaskGroup) {
            when (group.groupType) {
                GroupType.HEADER -> { bindHeader(group) }
                GroupType.GROUP -> { bindGroup(group) }
            }
        }

        // ####################
        // Header
        // ####################
        private fun bindHeader(header: TaskGroup) {
            // debugMessagePrint("Binded header")
            itemView.txtHeader.text = header.label
        }

        // ####################
        // Group entry
        // ####################
        private fun bindGroup(group: TaskGroup) {
            // debugMessagePrint("Binded group")

            // Assign date label
            itemView.labelDate.text = group.date.asString()         // Day number + Month: 1st May
            itemView.labelDay.apply {                               // Day of week: Mo...Su
                text = group.date.dayNameShort()
                // When day label clicked, call ActivityMain click listener function (De/Select entire group)
                setOnClickListener { if (group.isFoldedOut()) dateClicked(adapterPosition) }
            }
            // Update view if collapsed/expanded
            setFold(group)

            // Update view if collapsed/expanded (Clicked)
            itemView.btnFold.setOnClickListener {
                val newState: Fold = group.toggleFold()
                setFold(group)

                // Update collapsed counts, scroll position if expanded, ensure entire group visible
                when (newState) {
                    Fold.OUT -> {
                        scrollTo(adapterPosition)
                        Tracker.numFoldedIn--
                    }
                    Fold.IN -> Tracker.numFoldedIn++
                }
                updateExpandCollapseIcon()

                // Save change to view state
                updateSave()
            }

            // Assign layout manager + adapter
            itemView.rvTaskGroup.apply {
                layoutManager = LinearLayoutManager(itemView.rvTaskGroup.context, RecyclerView.VERTICAL, false)
                adapter = TasksAdapter(group, taskClicked, updateSave)
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
    fun allCollapsed() : Boolean { return Tracker.numFoldedIn == taskGroupList.size }

    // ########## Group related functionality ##########
    fun addTask(date: TaskDate, task: Task) {
        Tracker.taskCount++

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
        newTask.group = date.id

        taskGroupList.add(pos, TaskGroup(date, arrayListOf(newTask)))
        notifyItemInserted(pos)

        // Update collapse/expand icon to enable collapsing as new entry will always be expanded
        changeCollapseExpandIcon(Fold.OUT)
    }
    private fun addToTaskGroup(pos: Int, newTask: Task) {
        val group:TaskGroup = taskGroupList[pos]
        newTask.group = (group.date.id)

        group.taskList.add(newTask)
        notifyItemChanged(pos)
    }

    // ########## Modifying selected entries ##########
    fun delete() {
        // Delete all selected
        if (Tracker.allSelected()) {
            // Empty everything and reset values
            taskGroupList.clear()
            Tracker.clearAll()
            minDate = baseMinDate
            notifyDataSetChanged()
            return
        }

        // Update tracker values
        var groupDeleted = false
        Tracker.deleteSelected {
            val selectedTasks: ArrayList<SelectedTask> = Tracker.getSelected()
            // 1. Go through each group to check if they have tasks to delete
            for (groupNum: Int in taskGroupList.size - 1 downTo 0) {
                val group = taskGroupList[groupNum]

                // Group has tasks to delete
                if (group.numSelected != 0) {
                    // A. Filter selected tasks to obtain tasks to delete
                    val currGroup = selectedTasks.filter { it.group == group.date.id }
                    debugMessagePrint("To delete: $currGroup")

                    // B. Iterate through selected list (bottom to top)
                    for (taskNum: Int in currGroup.size - 1 downTo 0) {
                        group.taskList.removeAt(currGroup[taskNum].pos)
                        notifyItemChanged(groupNum)
                    }

                    // B. Set group to have none selected and unmark selected tasks
                    group.numSelected = 0
                    selectedTasks.removeAll(currGroup)

                    // C. Check if entire group needs to be deleted
                    if (group.isEmpty()) {
                        taskGroupList.removeAt(groupNum)
                        notifyItemRemoved(groupNum)
                        groupDeleted = true
                    }
                }

                // Exit early once all selected tasks have been removed
                if (selectedTasks.size == 0) break
            }
        }

        // Check to see if collapse/expand all icon needs to be updated (from group deletion)
        if (groupDeleted) {
            // Update minDate if removed date was the minimum one
            if (taskGroupList[0].date.id > minDate)
                minDate = taskGroupList[0].date.id

            updateExpandCollapseIcon()
        }
    }

    // ToDo
    fun setTagForSelected(newTag: Int) {
        /*
        // Uses same logic as delete(). We don't track group size in this case.
        for (groupNum: Int in taskGroupList.size - 1 downTo 0) {
            val group: TaskGroup = taskGroupList[groupNum]
            if (group.numSelected != 0) {
                group.selectedSetTag(data, newTag)
                notifyItemChanged(groupNum)
                if (data.numSelected == 0) break
            }
        }
        */
    }
    // ToDo
    fun setTimeForSelected(newTime: TaskTime) {
        /*
        for (groupNum: Int in taskGroupList.size - 1 downTo 0) {
            val group: TaskGroup = taskGroupList[groupNum]
            if (group.numSelected != 0) {
                group.selectedSetTime(data, newTime)
                notifyItemChanged(groupNum)
                if (data.numSelected == 0) break
            }
        }
        */
    }
    // ToDo
    fun setDateForSelected(newDate: TaskDate) {
        // Store list of tasks to be changed
        val movedTasks: ArrayList<Task> = arrayListOf()
        // val selectedTasks: ArrayList<Task> = Tracker.selectedTasks
    }
    /*
    fun setDateForSelected(newDate: TaskDate) {
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
            addNewTaskGroup(0, newDate, movedTasks[0])
            for (taskNum in 1 until movedTasks.size) {
                addToTaskGroup(0, movedTasks[taskNum])
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
    */

    // ########## Toggling ##########
    fun toggleGroupSelected(groupNum : Int) : Int {
        val group: TaskGroup = taskGroupList[groupNum]
        val numSelectedPreToggle:Int = group.numSelected

        group.toggleSelected()
        val difference: Int = group.numSelected - numSelectedPreToggle

        notifyItemChanged(groupNum)
        return difference
    }

    fun toggleSelectAll(selectAll : Boolean = true) {
        when (selectAll) {
            true -> Tracker.selectAll(taskGroupList)
            else -> Tracker.deselectAll()
        }
        notifyDataSetChanged()
    }
    fun toggleFoldAll(newState: Fold = Fold.OUT) {
        val end: Int = taskGroupList.size - 1
        for (groupNum in end downTo 0)
            taskGroupList[groupNum].state = newState
        notifyDataSetChanged()

        // Update collapsed count, 0 when all groups expanded, and maximum count when all collapsed
        Tracker.numFoldedIn = when (newState) {
            Fold.OUT -> 0
            Fold.IN -> taskGroupList.size
        }
    }

    private fun updateExpandCollapseIcon() {
        // Update icon accordingly based on number collapsed
        when (Tracker.numFoldedIn) {
            taskGroupList.size - 1 -> changeCollapseExpandIcon(Fold.OUT)  // Expandable
            taskGroupList.size -> changeCollapseExpandIcon(Fold.IN)       // All collapsed
        }
        // Ensure area occupied by grid is resized when row closed
        if (Settings.mainLayout == ViewLayout.GRID) notifyDataSetChanged()
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
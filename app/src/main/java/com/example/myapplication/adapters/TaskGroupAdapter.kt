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
import kotlin.math.abs
import kotlin.math.sign

class TaskGroupAdapter(private val taskGroupList: ArrayList<TaskGroup>,
                       private val taskClicked: (Task) -> Unit,
                       private val dateClicked: (Int) -> Unit,
                       private val scrollTo: (Int) -> Unit,
                       private val changeCollapseExpandIcon: (Fold) -> Unit,
                       private val updateSave: () -> Unit)
    : RecyclerView.Adapter<TaskGroupAdapter.ViewHolder>() {

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

        // Do nothing if its a blank list, otherwise override default values based on loaded list
        if (taskGroupList.isNotEmpty()) {
            // 1. Given save exists, update values based on previously saved list
            for (group: TaskGroup in taskGroupList) {
                // Go through each group to get taskCount and numCollapsed.
                DataTracker.taskCount += group.taskList.size
                if (!group.isFoldedOut()) DataTracker.numFoldedIn++

                // Clear previous selections
                group.setSelected(false)
            }

            // Check if expand collapse icon needs updating
            updateExpandCollapseIcon()
        }
    }

    // ##############################
    // Headers
    // ##############################
    private fun TaskGroup.assignHeader() : Boolean {
        // If all headers have been assigned, exit
        if (headersAssigned == headers.size) {
            debugMessagePrint("All headers assigned")
            return false
        }

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
    private fun removeHeader(period: Period) {
        headersAssigned--
        headers[period] = false
    }

    // Number of items in table view
    override fun getItemCount(): Int { return taskGroupList.size }
    // When group made, apply updates to UI based on internal values
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) { holder.bind(taskGroupList[pos]) }
    // Creating cell (date group entry)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Determine view to inflate: header or standard row
        val inflatedView: View = when (viewType) {
            GroupType.GROUP.ordinal -> parent.inflate(R.layout.task_group_rv)   // Group
            else -> parent.inflate(R.layout.task_group_header)                  // Header
        }
        return ViewHolder(inflatedView)
    }
    // 0 = Header, 1 = Standard Group
    override fun getItemViewType(position: Int): Int { return taskGroupList[position].groupType.ordinal }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // Bind content based on view passed in
        fun bind(group: TaskGroup) {
            when (group.groupType) {
                GroupType.HEADER -> bindHeader(group)
                GroupType.GROUP  -> bindGroup(group)
            }
        }

        // ####################
        // Header
        // ####################
        private fun bindHeader(header: TaskGroup) {
            itemView.txtHeader.text = header.label
        }

        // ####################
        // Group entry
        // ####################
        private fun bindGroup(group: TaskGroup) {
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
                        DataTracker.numFoldedIn--
                    }
                    Fold.IN -> DataTracker.numFoldedIn++
                }
                notifyItemChanged(adapterPosition)
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

    // ########## Group related functionality ##########
    fun addTask(date: TaskDate, task: Task) { addTask(date, arrayListOf(task)) }    // Singular Task
    private fun addTask(date: TaskDate, tasks: ArrayList<Task>) {
        DataTracker.taskCount += tasks.size

        // Bug fix. Clearing selection for newly added tasks
        for (task: Task in tasks) task.selected = false

        // 1. First group entry, always add to top
        if (taskGroupList.isEmpty()) {
            addNewTaskGroup(0, date, tasks)
            return
        }
        // 2. Second groupEntry. No need to sift through, as both min/max will be first entry (at [1])
        if (taskGroupList.size == 2) {
            // Comparing date with first entry
            val first: TaskDate = taskGroupList[1].date
            when {
                // A. Same. Append to first task
                date.id == first.id -> { addToTaskGroup(1, tasks) ; return }
                // B. Future. Append new group to end
                date.id > first.id -> { addNewTaskGroup(taskGroupList.size, date, tasks) ; return }
                // C. Past. Add before current date
                else -> {
                    // Check if header matches up. Otherwise need to add 1 pos before it
                    if (date.getPeriod() == taskGroupList[0].period) {
                        taskGroupList.add(1, TaskGroup(date, tasks))
                        notifyItemInserted(1)
                        changeCollapseExpandIcon(Fold.OUT)
                    }
                    // Otherwise insert at top position (header will be generated)
                    else { addNewTaskGroup(0, date, tasks) }
                    return
                }
            }
        }

        // ---------- Auto Sorting Entries ----------
        /* 2. Determine whether we start from top or bottom of list
         * - IF Top = Move downwards   - [0] will always be a header, so we start at [1]
         * - IF Bot = Move upwards     - [LastIndex] will always be a task
         */
        val top: TaskDate = taskGroupList[1].date
        val bot: TaskDate = taskGroupList[taskGroupList.lastIndex].date
        // Difference in days when compared to top and bottom value. Lower distance = starting pos
        val deltaTop: Int = dateDiff(top, date)
        val deltaBot: Int = dateDiff(bot, date)

        when (abs(deltaTop) < abs(deltaBot)) {
            true  -> addFromTop(date, tasks, deltaTop.sign)
            false -> addFromBottom(date, tasks, deltaBot.sign)
        }
    }               // Multiple Tasks
    private fun addFromBottom(date: TaskDate, tasks: ArrayList<Task>, direction: Int) {
        when (direction) {
            0 -> { addToTaskGroup(taskGroupList.lastIndex, tasks) }      // Matching date, append to bottom
            1 -> { addNewTaskGroup(taskGroupList.size, date, tasks) }    // Future date, add new bottom

            // Past date, iterate and move upwards
            -1 -> {
                for (pos: Int in taskGroupList.lastIndex downTo 0) {
                    val group: TaskGroup = taskGroupList[pos]
                    // Skip headers
                    if (group.groupType == GroupType.GROUP) {
                        // Matching date, append to existing list of tasks
                        if (group.date.id == date.id) {
                            addToTaskGroup(pos, tasks) ; return
                        }
                        // Date reached is earlier, create new date category with new task
                        if (group.date.id < date.id) {
                            addNewTaskGroup(pos + 1, date, tasks) ; return
                        }
                    }
                }
            }
        }
    }
    private fun addFromTop(date: TaskDate, tasks: ArrayList<Task>, direction: Int) {
        when (direction) {
            // Matching date, append to first pos
            0 -> { addToTaskGroup(1, tasks) }
            // Earlier date, add new group at top
            -1 -> {
                // Given first pos == header, check if matches same period of new task
                if (date.getPeriod() == taskGroupList[0].period) {
                    taskGroupList.add(1, TaskGroup(date, tasks))
                    notifyItemInserted(1)
                    changeCollapseExpandIcon(Fold.OUT)
                }
                // Otherwise insert at top position (header will be generated)
                else { addNewTaskGroup(0, date, tasks) }
            }

            // Future date, iterate and move downwards
            1 -> {
                for (pos: Int in 1 until taskGroupList.size) {
                    val group: TaskGroup = taskGroupList[pos]
                    // Check headers
                    when (group.groupType) {
                        // Check first task following header
                        GroupType.HEADER -> {
                            // Matching date, append to existing list of tasks
                            if (taskGroupList[pos+1].date.id == date.id) {
                                addToTaskGroup(pos, tasks)
                                return
                            }
                            // Date reached is later, add new group before it
                            if (taskGroupList[pos+1].date.id > date.id) {
                                addNewTaskGroup(pos, date, tasks)
                                return
                            }
                        }
                        // Same logic as above, except checking exact group
                        GroupType.GROUP -> {
                            if (group.date.id == date.id) { addToTaskGroup(pos, tasks) ; return }
                            if (group.date.id > date.id)  { addNewTaskGroup(pos, date, tasks) ; return }
                        }
                    }
                }
            }
        }
    }

    private fun addNewTaskGroup(pos: Int, date: TaskDate, tasks: ArrayList<Task>) {
        val newGroup = TaskGroup(date, tasks)

        // 1. Add group
        taskGroupList.add(pos, newGroup)
        notifyItemInserted(pos)

        // 2. Check if header needs to be added
        if (newGroup.assignHeader()) {
            val newHeader: TaskGroup = newGroup.createHeader()
            taskGroupList.add(pos, newHeader)
            notifyItemInserted(pos)
        }

        // Update collapse/expand icon to enable collapsing as new entry will always be expanded
        changeCollapseExpandIcon(Fold.OUT)
    }
    private fun addToTaskGroup(pos: Int, tasks: ArrayList<Task>) {
        // Appending an existing group
        taskGroupList[pos].taskList.addAll(tasks)
        notifyItemChanged(pos)
    }

    // ########## Modifying selected entries ##########
    fun delete() {
        // A. All selected, delete everything
        if (DataTracker.allSelected()) {
            clearAll()
            DataTracker.taskCount = 0
        }
        // B. Otherwise delete individual tasks
        else {
            // I. Delete tasks
            val deletedGroups: ArrayList<TaskGroup> = arrayListOf()
            for (groupNum: Int in taskGroupList.lastIndex downTo 0) {
                val group: TaskGroup = taskGroupList[groupNum]
                // If group has children selected, perform deletion
                if (group.numSelected != 0) {
                    group.selectedDelete()
                    notifyItemChanged(groupNum)

                    // Check if group (and header) needs to be deleted. If so add to list
                    deletedGroups.addAll(deleteGroup(groupNum))

                    // Once all selected tasks deleted, exit early
                    if (DataTracker.numSelected == 0) break
                }
            }
            // II. Delete marked groups/headers
            if (deletedGroups.isNotEmpty()) {
                taskGroupList.removeAll(deletedGroups)
                notifyDataSetChanged()
                updateExpandCollapseIcon()
            }
        }
    }
    private fun deleteGroup(groupNum: Int) : ArrayList<TaskGroup> {
        val group: TaskGroup = taskGroupList[groupNum]
        val deleted: ArrayList<TaskGroup> = arrayListOf()

        // When number of children == 0, group needs to be deleted
        if (group.isEmpty()) {
            // 1. Store group to be deleted
            deleted.add(group)
            // 2. Check if we need to store header provided task above group is a header
            val groupAbove: TaskGroup = taskGroupList[groupNum - 1]
            if (groupAbove.isHeader()) {
                // Check whether header above deleted group needs to be deleted
                val removeHeader: Boolean = when (groupNum) {
                    // Group = last index. Above it is header so header itself needs to be removed
                    taskGroupList.lastIndex -> true
                    // Any other index
                    else -> {
                        // Below == header, mark for removal (Both above and below is header)
                        if (taskGroupList[groupNum + 1].isHeader()) {
                            true
                        }
                        // Otherwise below == group, don't mark
                        else { return deleted }
                    }
                }
                if (removeHeader) {
                    deleted.add(groupAbove)
                    removeHeader(groupAbove.period)
                }
            }
        }
        return deleted
    }

    fun selectedSetTag(newTag: Int) {
        // Uses same logic as delete(). We don't track group size in this case.
        for (groupNum: Int in taskGroupList.size - 1 downTo 0) {
            val group: TaskGroup = taskGroupList[groupNum]
            if (group.numSelected != 0) {
                group.selectedSetTag(newTag)
                notifyItemChanged(groupNum)
                if (DataTracker.numSelected == 0) break
            }
        }
    }
    fun selectedSetTime(newTime: TaskTime) {
        for (groupNum: Int in taskGroupList.size - 1 downTo 0) {
            val group: TaskGroup = taskGroupList[groupNum]
            if (group.numSelected != 0) {
                group.selectedSetTime(newTime)
                notifyItemChanged(groupNum)
                if (DataTracker.numSelected == 0) break
            }
        }
    }
    fun selectedSetDate(newDate: TaskDate) {
        // Store list of tasks to be changed
        val movedTasks: ArrayList<Task> = arrayListOf()
        // Check number of tasks selected
        when (DataTracker.allSelected()) {
            // 1. All tasks selected
            true -> {
                // 1. Copy over every task, clearing selections
                for (group: TaskGroup in taskGroupList) {
                    for (task: Task in group.taskList) {
                        task.selected = false
                        movedTasks.add(task)
                    }
                }
                // 2. Clear list, reset dataTracker and header data
                clearAll()
                // 3. Create new group at [0] and add all tasks to it. Header is to be generated
                addNewTaskGroup(0, newDate, movedTasks)
            }

            // 2. Specific tasks selected
            false -> {
                // 1. Copy over selected tasks
                for (group: TaskGroup in taskGroupList) {
                    for (task: Task in group.taskList) {
                        if (task.selected) movedTasks.add(task)
                    }
                }
                // 2. Delete selected tasks at their previous positions
                delete()
                // 3. Re-add moved tasks to new group
                addTask(newDate, movedTasks)
            }
        }
    }

    private fun clearAll() {
        // Empty everything and reset values
        taskGroupList.clear()                   // Clear entire group list
        DataTracker.numSelected = 0             // Reset selected count

        // Reset header parameters
        for (period: Period in headers.keys) headers[period] = false
        headersAssigned = 0

        notifyDataSetChanged()
    }

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
        val end: Int = taskGroupList.size - 1
        for (groupNum: Int in end downTo 0)
            taskGroupList[groupNum].setSelected(selectAll)

        notifyDataSetChanged()
    }
    fun toggleFoldAll(newState: Fold = Fold.OUT) {
        for (index: Int in taskGroupList.lastIndex downTo 0) {
            val group: TaskGroup = taskGroupList[index]
            if (!group.isHeader()) {    // Toggle fold state, ignoring headers
                group.state = newState
                notifyItemChanged(index)
            }
        }
        // Update collapsed count, 0 when all groups expanded, and maximum count when all collapsed
        DataTracker.numFoldedIn = when (newState) {
            Fold.OUT -> 0
            Fold.IN -> DataTracker.taskCount
        }
    }

    private fun updateExpandCollapseIcon() {
        // Update icon accordingly based on number collapsed
        when (DataTracker.numFoldedIn) {
            DataTracker.taskCount - 1 -> changeCollapseExpandIcon(Fold.OUT)  // Expandable
            DataTracker.taskCount     -> changeCollapseExpandIcon(Fold.IN)   // All collapsed
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
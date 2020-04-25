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

            // Set minimum date to first entry and check if expand collapse icon needs updating
            minDate = taskGroupList[0].date.id
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
    private fun resetHeaders() {
        // Set all values to false
        for (period: Period in headers.keys)
            headers[period] = false
        headersAssigned = 0
    }
    private fun removeHeader(index: Int) {
        headersAssigned--
        headers[taskGroupList[index].period] = false
        taskGroupList.removeAt(index)
        notifyItemRemoved(index)
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
                GroupType.HEADER -> { bindHeader(group) }
                GroupType.GROUP  -> { bindGroup(group) }
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
    fun allCollapsed() : Boolean { return DataTracker.numFoldedIn == taskGroupList.size }

    // ########## Group related functionality ##########
    fun addTask(date: TaskDate, task: Task) {
        DataTracker.taskCount++

        // 1. First entry, always add to top
        if (taskGroupList.isEmpty()) {
            minDate = date.id
            addNewTaskGroup(0, date, task)
            return
        }
        // 2. Second entry. No need to sift through, as both min/max will be first entry (at [1])
        if (DataTracker.taskCount == 2) {
            // Comparing date with first entry
            val first: TaskDate = taskGroupList[1].date
            when {
                // A. Same. Append to first task
                date.id == first.id -> { addToTaskGroup(1, task) ; return }
                // B. Future. Append new group to end
                date.id > first.id -> { addNewTaskGroup(taskGroupList.size, date, task) ; return }
                // C. Past. Add before current date
                else -> {
                    // Check if header matches up. Otherwise need to add 1 pos before it
                    if (date.getPeriod() == taskGroupList[0].period) {
                        taskGroupList.add(1, TaskGroup(date, arrayListOf(task)))
                        notifyItemInserted(1)
                        changeCollapseExpandIcon(Fold.OUT)
                    }
                    // Otherwise insert at top position (header will be generated)
                    else { addNewTaskGroup(0, date, task) }
                    minDate = date.id
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
            true  -> addFromTop(date, task, deltaTop.sign)
            false -> addFromBottom(date, task, deltaBot.sign)
        }
    }
    private fun addFromBottom(date: TaskDate, task:Task, direction: Int) {
        when (direction) {
            0 -> { addToTaskGroup(taskGroupList.lastIndex, task) }      // Matching date, append to bottom
            1 -> { addNewTaskGroup(taskGroupList.size, date, task) }    // Future date, add new bottom

            // Past date, iterate and move upwards
            -1 -> {
                for (pos: Int in taskGroupList.lastIndex downTo 0) {
                    val group: TaskGroup = taskGroupList[pos]
                    // Skip headers
                    if (group.groupType == GroupType.GROUP) {
                        // Matching date, append to existing list of tasks
                        if (group.date.id == date.id) {
                            addToTaskGroup(pos, task) ; return
                        }
                        // Date reached is earlier, create new date category with new task
                        if (group.date.id < date.id) {
                            addNewTaskGroup(pos + 1, date, task) ; return
                        }
                    }
                }
            }
        }
    }
    private fun addFromTop(date: TaskDate, task:Task, direction: Int) {
        when (direction) {
            // Matching date, append to first pos
            0 -> { addToTaskGroup(1, task) }
            // Earlier date, add new group at top
            -1 -> {
                // Given first pos == header, check if matches same period of new task
                if (date.getPeriod() == taskGroupList[0].period) {
                    taskGroupList.add(1, TaskGroup(date, arrayListOf(task)))
                    notifyItemInserted(1)
                    changeCollapseExpandIcon(Fold.OUT)
                }
                // Otherwise insert at top position (header will be generated)
                else { addNewTaskGroup(0, date, task) }
                minDate = date.id
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
                                addToTaskGroup(pos, task)
                                return
                            }
                            // Date reached is later, add new group before it
                            if (taskGroupList[pos+1].date.id > date.id) {
                                addNewTaskGroup(pos, date, task)
                                return
                            }
                        }
                        // Same logic as above, except checking exact group
                        GroupType.GROUP -> {
                            if (group.date.id == date.id) { addToTaskGroup(pos, task) ; return }
                            if (group.date.id > date.id)  { addNewTaskGroup(pos, date, task) ; return }
                        }
                    }
                }
            }
        }
    }

    private fun addNewTaskGroup(pos: Int, date: TaskDate, newTask: Task) {
        val newGroup = TaskGroup(date, arrayListOf(newTask))

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
    private fun addToTaskGroup(pos: Int, newTask: Task) {
        // Appending an existing group
        taskGroupList[pos].taskList.add(newTask)
        notifyItemChanged(pos)
    }

    // ########## Modifying selected entries ##########
    fun delete() {
        // Delete all selected
        if (DataTracker.allSelected()) {
            // Empty everything and reset values
            taskGroupList.clear()
            DataTracker.deleteSelected()
            minDate = baseMinDate
            resetHeaders()
            notifyDataSetChanged()
            return
        }

        // Otherwise go through groups
        var groupDeleted = false
        for (groupNum: Int in taskGroupList.lastIndex downTo 0) {
            val group: TaskGroup = taskGroupList[groupNum]
            // If group has children selected, perform deletion
            if (group.numSelected != 0) {
                group.selectedDelete()
                notifyItemChanged(groupNum)

                // Check if group needs to be deleted. In turn whether header needs to be deleted
                groupDeleted = checkGroupDelete(groupNum)

                // Once all selected tasks deleted, exit early
                if (DataTracker.numSelected == 0) break
            }
        }

        // Check to see if collapse/expand all icon needs to be updated (from group deletion)
        if(groupDeleted) { updateExpandCollapseIcon() }
        notifyDataSetChanged()
    }
    private fun checkGroupDelete(groupNum: Int) : Boolean {
        val group: TaskGroup = taskGroupList[groupNum]

        // When number of children == 0, group needs to be deleted
        if (group.isEmpty()) {
            val lastIndex: Int = taskGroupList.lastIndex

            // 1. Update minDate if group's date is minimum one
            if (group.date.id == minDate) {
                val nextGroup:Int = groupNum + 1
                // Set minDate to next group, if its a header, set minDate to group after it
                minDate =
                    if (taskGroupList[nextGroup].isHeader())
                        taskGroupList[nextGroup + 1].date.id
                    else
                        taskGroupList[nextGroup].date.id
            }

            // 2. Remove Group
            taskGroupList.removeAt(groupNum)
            notifyItemRemoved(groupNum)

            // Determine groups above/below after deletion
            val above: Int = groupNum - 1
            val groupAbove: TaskGroup = taskGroupList[above]

            // 3. Check if we need to remove header provided task above is a header
            if (groupAbove.isHeader()) {
                when (groupNum) {
                    // Group = last index. After deletion, header will be empty
                    lastIndex -> removeHeader(above)
                    // Any other index
                    else -> {
                        // Not groupNum + 1. Due to deletion, below index shifted up to be same as groupNum
                        val below: Int = groupNum
                        // Both Above and Below is a header, remove Above header of deleted group
                        if (taskGroupList[below].isHeader()) { removeHeader(above) }
                    }
                }
            }

            // Group was deleted
            return true
        }

        return false
    }

    fun setTagForSelected(newTag: Int) {
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
    fun setTimeForSelected(newTime: TaskTime) {
        for (groupNum: Int in taskGroupList.size - 1 downTo 0) {
            val group: TaskGroup = taskGroupList[groupNum]
            if (group.numSelected != 0) {
                group.selectedSetTime(newTime)
                notifyItemChanged(groupNum)
                if (DataTracker.numSelected == 0) break
            }
        }
    }
    fun setDateForSelected(newDate: TaskDate) {
        // Store list of tasks to be changed
        val movedTasks: ArrayList<Task> = arrayListOf()

        // Case 1: All selected, move everything
        if (DataTracker.allSelected()) {
            // Copy over every task
            for (group in taskGroupList) { movedTasks.addAll(group.taskList) }

            // Clear DataTracker, set new date as minimum date (as it will be the only one)
            DataTracker.numSelected = 0
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
                    DataTracker.taskCount -= DataTracker.numSelected
                    DataTracker.numSelected -= group.taskList.count()
                    notifyItemChanged(groupNum)

                    if (DataTracker.numSelected == 0) break
                }
                // B. Between 1 - group size selected, copy only selected tasks and delete them
                else if (group.numSelected >= 0) {
                    for (taskNum: Int in group.taskList.size - 1 downTo 0) {
                        if (group.taskList[taskNum].selected) {
                            movedTasks.add(group.taskList[taskNum])
                            group.taskList.removeAt(taskNum)
                            group.numSelected--
                            DataTracker.numSelected--
                            DataTracker.taskCount--
                        }
                    }
                    notifyItemChanged(groupNum)
                    if (DataTracker.numSelected == 0) break
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
        val numSelectedPreToggle:Int = group.numSelected

        group.toggleSelected()
        val difference: Int = group.numSelected - numSelectedPreToggle

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
        DataTracker.numFoldedIn = when (newState) {
            Fold.OUT -> 0
            Fold.IN -> taskGroupList.size
        }
    }

    private fun updateExpandCollapseIcon() {
        // Update icon accordingly based on number collapsed
        when (DataTracker.numFoldedIn) {
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
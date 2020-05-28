package com.example.myapplication.adapters

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data_classes.*
import com.example.myapplication.utility.*
import kotlinx.android.synthetic.main.task_group_header.view.*
import kotlinx.android.synthetic.main.task_group_rv.view.*
import kotlin.math.abs
import kotlin.math.sign


class TaskGroupAdapter(
     private val taskGroupList: ArrayList<GroupEntry>,
     private val taskClicked: (Task) -> Unit,
     private val dateClicked: (Int) -> Unit,
     private val scrollTo: (Int) -> Unit,
     private val changeCollapseExpandIcon: (Fold) -> Unit,
     private val updateSave: () -> Unit) : RecyclerView.Adapter<TaskGroupAdapter.ViewHolder>()
{
    // ##############################
    // Headers
    // ##############################
    private val headers = hashMapOf (
        // Key: [Period], Value = [Boolean]
        Week.PAST      to false,
        Week.THIS      to false,
        Week.NEXT      to false,
        Week.FORTNIGHT to false,
        Week.FUTURE    to false
    )
    private var headersAssigned = 0

    private fun TaskGroup.checkAddHeader() : Boolean{
        // If all headers have been assigned, exit
        if (headersAssigned == headers.size) return false

        // 1. Get week group belongs to
        val week: Week = date.getWeek()
        // 2. Check if week assigned yet, if not mark to assign, update counter
        if (headers[week] == false) {
            headers[week] = true
            headersAssigned++
            return true
        }

        return false
    }
    private fun removeHeader(week: Week) {
        headersAssigned--
        headers[week] = false
    }

    // Initialization
    init {
        // Remove older entries, requires setting toggled on
        // if (Settings.deleteOldDates) { deleteOldTasks() }

        // Override default values based on loaded list if not empty
        if (taskGroupList.isNotEmpty()) {
            // List of tasks to rearrange
            val sortedList: ArrayList<GroupEntry> = arrayListOf()
            // Given save exists, update values based on previously saved list
            for (entry: GroupEntry in taskGroupList) {
                when (entry.type) {
                    GroupType.GROUP -> {
                        val group: TaskGroup = entry.taskGroup!!

                        // 1. Go through each group to get taskCount and numCollapsed. Clear prev selections
                        DataTracker.taskCount += group.taskList.size
                        if (!group.isFoldedOut()) DataTracker.numFoldedIn++
                        group.setSelected(false)

                        // 2A. Check if header has been added yet, if not add header
                        if (group.checkAddHeader()) { sortedList.add(headerEntry(group.date.getWeek())) }
                        // 2B. Add copy of task
                        sortedList.add(entry)
                    }
                    GroupType.HEADER -> { }
                }
            }
            // Override current list with correctly sorted headers
            taskGroupList.clear()
            taskGroupList.addAll(sortedList)
            // Check if expand collapse icon needs updating
            updateExpandCollapseIcon()
        }
    }

    // ##############################
    // Standard RecyclerView Functions
    // ##############################
    // Number of items in table view
    override fun getItemCount(): Int { return taskGroupList.size }
    // When group made, apply updates to UI based on internal values
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        when (taskGroupList[pos].type) {
            GroupType.HEADER -> holder.bindHeader(taskGroupList[pos].header!!)
            GroupType.GROUP -> holder.bindGroup(taskGroupList[pos].taskGroup!!)
        }
    }
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
    override fun getItemViewType(position: Int): Int { return taskGroupList[position].type.ordinal }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // ####################
        // Header
        // ####################
        fun bindHeader(header: GroupHeader) { itemView.txtHeader.text = header.label }

        // ####################
        // Group entry
        // ####################
        fun bindGroup(group: TaskGroup) {
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
                        itemView.btnFold.setImageResource(R.drawable.arrow_down)
                    }

                    Fold.IN -> {
                        // Close group, and change icon
                        itemView.rvTaskGroup.visibility = View.GONE
                        itemView.btnFold.setImageResource(R.drawable.arrow_up)
                    }
                }
            }

            // Toggling arrow icon highlighting when group collapsed
            if (group.state == Fold.IN) {
                // If a task has been selected, highlight background to indicate
                when (group.numSelected) {
                    0 -> itemView.btnFold.applyBackgroundColor(Color.TRANSPARENT)
                    // Clear highlights (via selectAll toggle when collapsed)
                    else -> itemView.btnFold.applyBackgroundColor(Settings.highlightColor)
                }

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
            // Comparing date with first entry. Value in this position will always be a taskGroup (not header)
            val first: TaskDate = taskGroupList[1].taskGroup!!.date
            when {
                // A. Same. Append to first task
                date.id == first.id -> { addToTaskGroup(1, tasks) ; return }
                // B. Future. Append new group to end
                date.id > first.id -> { addNewTaskGroup(taskGroupList.size, date, tasks) ; return }
                // C. Past. Add before current date
                else -> {
                    // Check if header matches up. Otherwise need to add 1 pos before it
                    if (date.getWeek() == taskGroupList[0].header!!.week) {
                        taskGroupList.add(1, taskGroupEntry(date, tasks))
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
        val top: TaskDate = taskGroupList[1].taskGroup!!.date
        val bot: TaskDate = taskGroupList[taskGroupList.lastIndex].taskGroup!!.date
        // Difference in days when compared to top and bottom value. Lower distance = starting pos
        val deltaTop: Int = dateDiff(top, date)
        val deltaBot: Int = dateDiff(bot, date)

        when (abs(deltaTop) < abs(deltaBot)) {
            true  -> addFromTop(date, tasks, deltaTop.sign)
            false -> addFromBottom(date, tasks, deltaBot.sign)
        }
    }
    private fun addFromBottom(date: TaskDate, tasks: ArrayList<Task>, direction: Int) {
        when (direction) {
            0 -> { addToTaskGroup(taskGroupList.lastIndex, tasks) }      // Matching date, append to bottom
            1 -> { addNewTaskGroup(taskGroupList.size, date, tasks) }    // Future date, add new bottom

            // Past date, iterate and move upwards
            -1 -> {
                for (pos: Int in taskGroupList.lastIndex downTo 0) {
                    val entry: GroupEntry = taskGroupList[pos]
                    // Skip headers
                    if (entry.type == GroupType.GROUP) {
                        val groupID: Int = entry.taskGroup!!.date.id

                        // Matching date, append to existing list of tasks
                        if (groupID == date.id) {
                            addToTaskGroup(pos, tasks) ; return
                        }
                        // Date reached is earlier, create new date category with new task
                        if (groupID < date.id) {
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
                // Given first pos == header, check if matches same week of new task
                if (date.getWeek() == taskGroupList[0].header!!.week) {
                    taskGroupList.add(1, taskGroupEntry(date, tasks))
                    notifyItemInserted(1)
                    changeCollapseExpandIcon(Fold.OUT)
                }
                // Otherwise insert at top position (header will be generated)
                else { addNewTaskGroup(0, date, tasks) }
            }
            // Future date, iterate and move downwards
            1 -> {
                for (pos: Int in 1 until taskGroupList.size) {
                    val entry: GroupEntry = taskGroupList[pos]
                    // Check headers
                    when (entry.type) {
                        // Check first task following header
                        GroupType.HEADER -> {
                            val nextEntryDate: Int = taskGroupList[pos+1].taskGroup!!.date.id
                            // Matching date, append to existing list of tasks
                            if (nextEntryDate == date.id) {
                                addToTaskGroup(pos, tasks)
                                return
                            }
                            // Date reached is later, add new group before it
                            if (nextEntryDate > date.id) {
                                addNewTaskGroup(pos, date, tasks)
                                return
                            }
                        }
                        // Same logic as above, except checking exact group
                        GroupType.GROUP -> {
                            val currGroupID: Int = entry.taskGroup!!.date.id
                            if (currGroupID == date.id) { addToTaskGroup(pos, tasks) ; return }
                            if (currGroupID > date.id)  { addNewTaskGroup(pos, date, tasks) ; return }
                        }
                    }
                }
            }
        }
    }

    private fun addNewTaskGroup(pos: Int, date: TaskDate, tasks: ArrayList<Task>) {
        val newGroup: GroupEntry = taskGroupEntry(date, tasks)

        // 1. Add group
        taskGroupList.add(pos, newGroup)
        notifyItemInserted(pos)

        // 2. Check if header needs to be added
        if (newGroup.taskGroup!!.checkAddHeader()) {
            // Add new header before designated group
            taskGroupList.add(pos, headerEntry(date.getWeek()))
            notifyItemInserted(pos)
        }

        // Update collapse/expand icon to enable collapsing as new entry will always be expanded
        changeCollapseExpandIcon(Fold.OUT)
    }
    private fun addToTaskGroup(pos: Int, tasks: ArrayList<Task>) {
        // Appending an existing group
        taskGroupList[pos].taskGroup!!.taskList.addAll(tasks)
        notifyItemChanged(pos)
    }

    private fun above(pos:Int): GroupEntry {
        return when (pos) {
            0    -> taskGroupList[0]        // At topmost position (nothing above), return self
            else -> taskGroupList[pos - 1]  // Return entry above it
        }
    }

    // ########## Modifying selected entries ##########
    fun delete() {
        when (DataTracker.allSelected()) {
            // A. All selected, delete everything
            true -> {
                deleteAll()
                DataTracker.taskCount = 0
            }
            // B. Otherwise delete individual tasks
            false -> {
                // Delete tasks
                var groupDeleted = false
                for (groupNum: Int in taskGroupList.lastIndex downTo 0) {
                    val entry: GroupEntry = taskGroupList[groupNum]
                    when (entry.type) {
                        GroupType.HEADER -> {
                            var deleteHeader = false
                            // Check header's index
                            when (groupNum) {
                                // A. Last in list (No task group below)
                                taskGroupList.lastIndex -> deleteHeader = true
                                // B. Not last in list (Has group below). Check if below is header
                                else -> if (taskGroupList[groupNum+1].isHeader()) deleteHeader = true
                            }
                            // Check if header needs to be deleted (empty header)
                            if (deleteHeader) {
                                removeHeader(taskGroupList[groupNum].header!!.week)
                                taskGroupList.removeAt(groupNum)
                                notifyItemRemoved(groupNum)
                            }
                        }
                        GroupType.GROUP -> {
                            val group: TaskGroup = entry.taskGroup!!
                            // I. If group has children selected, perform deletion
                            if (group.numSelected != 0) {
                                group.selectedDelete()
                                notifyItemChanged(groupNum)

                                // II. Check if group needs to be deleted (all children have been deleted)
                                if (group.taskList.size == 0) {
                                    taskGroupList.removeAt(groupNum)
                                    notifyItemRemoved(groupNum)
                                    groupDeleted = true
                                }
                            }
                        }
                    }
                    // If above is a group and numSelected = 0, exit as we are done.
                    // Otherwise continue one more time to see if header needs to be deleted
                    if (DataTracker.numSelected == 0 && above(groupNum).isGroup()) break
                }
                // If any group has been deleted, expand/collapse icon needs to be updated
                if (groupDeleted) updateExpandCollapseIcon()
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
                for (entry: GroupEntry in taskGroupList) {
                    if (entry.isGroup()) {
                        for (task: Task in entry.taskGroup!!.taskList) {
                            task.selected = false
                            movedTasks.add(task)
                        }
                    }
                }
                // 2. Clear list, reset dataTracker and header data
                deleteAll()
                // 3. Create new group at [0] and add all tasks to it. Header is to be generated
                addNewTaskGroup(0, newDate, movedTasks)
            }

            // 2. Specific tasks selected
            false -> {
                // 1. Copy over selected tasks
                for (entry: GroupEntry in taskGroupList) {
                    if (entry.isGroup()) {
                        for (task: Task in entry.taskGroup!!.taskList) {
                            if (task.selected) movedTasks.add(task)
                        }
                    }
                }
                // 2. Delete selected tasks at their previous positions
                delete()
                // 3. Re-add moved tasks to new group
                addTask(newDate, movedTasks)
            }
        }
    }
    fun selectedSetTime(newTime: TaskTime) {
        for (groupNum: Int in taskGroupList.size - 1 downTo 0) {
            val entry: GroupEntry = taskGroupList[groupNum]
            if (entry.isGroup()) {
                val group: TaskGroup = entry.taskGroup!!
                if (group.numSelected != 0) {
                    group.selectedSetTime(newTime)
                    notifyItemChanged(groupNum)
                    if (DataTracker.numSelected == 0) break
                }
            }
        }
    }
    fun selectedSetTag(newTag: Int) {
        // Uses same logic as delete(). We don't track group size in this case.
        for (groupNum: Int in taskGroupList.size - 1 downTo 0) {
            val entry: GroupEntry = taskGroupList[groupNum]
            if (entry.isGroup()) {
                val group: TaskGroup = entry.taskGroup!!
                if (group.numSelected != 0) {
                    group.selectedSetTag(newTag)
                    notifyItemChanged(groupNum)
                    if (DataTracker.numSelected == 0) break
                }
            }
        }
    }
    fun selectedClearAll() {
        for (groupNum: Int in taskGroupList.size - 1 downTo 0) {
            val entry: GroupEntry = taskGroupList[groupNum]
            if (entry.isGroup()) {
                val group: TaskGroup = entry.taskGroup!!
                if (group.numSelected != 0) {
                    group.selectedClear()
                    notifyItemChanged(groupNum)
                    if (DataTracker.numSelected == 0) break
                }
            }
        }
    }

    private fun deleteAll() {
        // Empty everything and reset values
        taskGroupList.clear()                   // Clear entire group list
        DataTracker.numSelected = 0             // Reset selected count

        // Reset header parameters
        for (week: Week in headers.keys) headers[week] = false
        headersAssigned = 0

        notifyDataSetChanged()
    }

    // ########## Toggling ##########
    fun toggleGroupSelected(groupNum : Int) : Int {
        val group: TaskGroup = taskGroupList[groupNum].taskGroup!!
        val numSelectedPreToggle:Int = group.numSelected

        group.toggleSelected()
        val difference: Int = group.numSelected - numSelectedPreToggle

        notifyItemChanged(groupNum)
        return difference
    }

    fun toggleSelectAll(selectAll : Boolean = true) {
        val end: Int = taskGroupList.size - 1
        for (groupNum: Int in end downTo 0) {
            if (taskGroupList[groupNum].isGroup())
                taskGroupList[groupNum].taskGroup!!.setSelected(selectAll)
        }

        notifyDataSetChanged()
    }
    fun toggleFoldAll(newState: Fold = Fold.OUT) {
        for (index: Int in taskGroupList.lastIndex downTo 0) {
            val entry: GroupEntry = taskGroupList[index]
            if (entry.isGroup()) {    // Toggle fold state, ignoring headers
                entry.taskGroup!!.state = newState
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
    }

    // ########## Other ##########
    /*
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
    */
}
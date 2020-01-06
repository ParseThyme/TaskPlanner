package com.example.myapplication.adapters

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.Settings
import com.example.myapplication.addDivider
import com.example.myapplication.data_classes.Task
import com.example.myapplication.data_classes.TaskGroup
import com.example.myapplication.data_classes.groupSelected
import com.example.myapplication.inflate
import kotlinx.android.synthetic.main.task_group_rv.view.*

class AdapterTaskGroup(private val taskGroupList: ArrayList<TaskGroup>,
                       private val taskClickListener: (Task) -> Unit,
                       private val dateClickListener: (Int) -> Unit,
                       private val saveFunction: () -> Unit,
                       private val settings: Settings)
    : RecyclerView.Adapter<AdapterTaskGroup.ViewHolder>() {

    // Date changed for task
    private val changeDateListener = {
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

    // Creating cell
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Use inflate function found in Util then return containing cell layout and clickListener
        val inflatedView = parent.inflate(R.layout.task_group_rv, false)
        return ViewHolder(inflatedView)
    }

    // When cell made
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Assign description and date to task based on stored array
        holder.bind(taskGroupList[position], taskClickListener, dateClickListener, saveFunction, settings)
    }

    // ########## Group related functionality ##########
    fun addTask(id: Int, date: String, desc: String) {
        taskCount++

        // ---------- Auto Sorting Entries ----------
        // [A]. Check for earliest date
        if (id < minDate) {
            // New date is earlier, make it the new min date and insert new group at the top
            minDate = id
            addNewTaskGroup(0, date, Task(desc), id)
            return
        }

        // [B]. Otherwise start from latest entry and move upwards
        for (pos in taskGroupList.lastIndex downTo 0 step 1) {
            // [1]. Matching date, append to existing list of tasks
            if (id == taskGroupList[pos].id) {
                addToTaskGroup(pos, Task(desc))
                return
            }
            // [2]. Date reached is earlier, create new date category with new task
            if (id > taskGroupList[pos].id) {
                addNewTaskGroup(pos + 1, date, Task(desc), id)
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

    fun toggleGroup(groupNum : Int) : Int {
        var difference = 0
        val group: TaskGroup = taskGroupList[groupNum]

        if (groupSelected(group)) {
            for (i in 0 until group.taskList.size)
                group.taskList[i].selected = false

            group.numSelected = 0
            difference = -(group.taskList.size)
        }
        else {
            for (i in 0 until group.taskList.size) {
                val task: Task = group.taskList[i]
                if (!task.selected) {
                    difference += 1
                    group.numSelected += 1
                    task.selected = true

                    if (group.numSelected == group.taskList.size) {
                        break
                    }
                }
            }
        }

        notifyItemChanged(groupNum)
        return difference
    }

    fun toggleAll(selectAll : Boolean = true) {
        val end = taskGroupList.size - 1
        for (groupNum in end downTo 0) {
            val group = taskGroupList[groupNum]

            // Go through each individual task and select/deselect it
            for (taskNum in group.taskList.size - 1 downTo 0)
                group.taskList[taskNum].selected = selectAll

            // Depending on overall call, either set num selected to group count or 0
            if (selectAll)
                group.numSelected = group.taskList.size
            else
                group.numSelected = 0

            notifyItemChanged(groupNum)
        }
    }

    private fun changeGroup(task: Task, newDate: String, oldID: Int, newID: Int) {
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

        // "Move" to new position (add new task). -- to balance out addition made in addTask()
        addTask(newID, newDate, task.desc)
        taskCount--
    }

    // ########## ViewHolder ##########
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // Defining reference to task description text in layout
        private val tasksRV = itemView.taskGroupRV
        private val dateLabel = itemView.dateLabel

        fun bind(group: TaskGroup,
                 taskClickListener: (Task) -> Unit,
                 dateClickListener: (Int) -> Unit,
                 saveFunction: () -> Unit,
                 settings: Settings) {
            // Assign date label
            dateLabel.text = group.date

            // When date label clicked, call ActivityMain click listener function
            this.itemView.setOnClickListener { dateClickListener(adapterPosition) }

            // Store reference to task adapter
            val taskAdapter = AdapterTasks(group, taskClickListener, changeDateListener, saveFunction, settings)
            // Assign layout manager + adapter
            tasksRV.apply {
                layoutManager = LinearLayoutManager(tasksRV.context, RecyclerView.VERTICAL, false)
                adapter = taskAdapter
            }
        }
    }
}
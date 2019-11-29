package com.example.myapplication.adapters

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data_classes.Task
import com.example.myapplication.data_classes.TaskGroup
import com.example.myapplication.inflate
import kotlinx.android.synthetic.main.rv_taskgroup.view.*

class AdapterTaskGroup(private val taskGroupList: ArrayList<TaskGroup>,
                       private val clickListener: (Int, Task) -> Unit)
    : RecyclerView.Adapter<AdapterTaskGroup.ViewHolder>() {

    // Total task count (from entire recycler view)
    private var taskCount = 0

    // Used for sorting, default value ensures new min value is always replaced with first entry
    private val baseMinDate = 90000000
    private var minDate = baseMinDate

    // Number of items in table view
    override fun getItemCount(): Int { return taskGroupList.size }

    // Creating cell
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Use inflate function found in Util then return containing cell layout and clickListener
        val inflatedView = parent.inflate(R.layout.rv_taskgroup, false)
        return ViewHolder(inflatedView)
    }

    // When cell made
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Assign description and date to task based on stored array
        holder.bind(taskGroupList[position], clickListener)
    }

    // ########## Adding new task/task group ##########
    fun addTask(newId: Int, newDate: String, newDesc: String) {
        taskCount++

        // ---------- Auto Sorting Entries ----------
        // [A]. Check for earliest date
        if (newId < minDate) {
            // New date is earlier, make it the new min date and insert new group at the top
            minDate = newId
            addNewTaskGroup(0, newDate, Task(newDesc), newId)
            return
        }

        // [B]. Otherwise start from latest entry and move upwards
        for (pos in taskGroupList.lastIndex downTo 0 step 1) {
            // [1]. Matching date, append to existing list of tasks
            if (newId == taskGroupList[pos].id) {
                addToTaskGroup(pos, Task(newDesc))
                return
            }
            // [2]. Date reached is earlier, create new date category with new task
            if (newId > taskGroupList[pos].id) {
                addNewTaskGroup(pos + 1, newDate, Task(newDesc), newId)
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

    // ########## Deleting task entries ##########
    fun deleteTasks(selected : Int, deleteAll : Boolean = false) {
        // [1]. Clearing entire list
        if (deleteAll) {
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

    fun toggleSelectAll(selectAll : Boolean = true) {
        val end = taskGroupList.size - 1
        for (groupNum in end downTo 0) {
            val group = taskGroupList[groupNum]

            for (taskNum in group.taskList.size - 1 downTo 0) {
                group.taskList[taskNum].selected = selectAll
            }
            group.numSelected = group.taskList.size
            notifyItemChanged(groupNum)
        }
    }

    // ########## ViewHolder ##########
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // Defining reference to task description text in layout
        private val tasksRV = itemView.taskGroupRV
        private val dateLabel = itemView.dateLabel

        private lateinit var taskAdapter: AdapterTasks

        fun bind(taskGroup: TaskGroup, clickListener: (Int, Task) -> Unit) {
            // Assign date label
            dateLabel.text = taskGroup.date

            // Store reference to task adapter
            taskAdapter = AdapterTasks(taskGroup, clickListener)
            // Assign layout manager + adapter
            tasksRV.apply {
                layoutManager = LinearLayoutManager(tasksRV.context, RecyclerView.VERTICAL, false)
                adapter = taskAdapter
            }
        }
    }
}
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

class AdapterTaskGroup(private val taskGroupList: ArrayList<TaskGroup>, private val clickListener: (Int, Task) -> Unit)
    : RecyclerView.Adapter<AdapterTaskGroup.ViewHolder>() {

    // Selected tasks (mark as complete or delete)
    private var taskCount = 0
    private var numSelected = 0

    // Used for sorting, default value ensures new min value is always replaced with first entry
    private val baseMinDate = 90000000
    private var minDate = baseMinDate

    // Adapter referencing child tasks

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
        val newTask = Task(newDesc)
        taskCount++

        // ---------- Auto Sorting Entries ----------
        // [A]. Check for earliest date
        if (minDate > newId) {
            // New date is earlier, make it the new min date and insert new group at the top
            minDate = newId
            addNewTaskGroup(0, newDate, newTask, newId)
            return
        }

        // [B]. Otherwise start from latest entry and move upwards
        for (pos in taskGroupList.lastIndex downTo 0 step 1) {
            // [1]. Matching date, append to existing list of tasks
            if (taskGroupList[pos].id == newId) {
                Log.d("Test", "Matching date")
                addToTaskGroup(pos, newTask)
                return
            }
            // [2]. Date reached is earlier, create new date category with new task
            if (taskGroupList[pos].id < newId) {
                // insert(new, pos + 1)
                addNewTaskGroup(pos + 1, newDate, newTask, newId)
                return
            }
        }
    }

    private fun addNewTaskGroup(pos: Int, date: String, newTask: Task, id: Int) {
        taskGroupList.add(pos, TaskGroup(date, arrayListOf(newTask), id))
        notifyItemInserted(pos)
    }

    private fun addToTaskGroup(pos: Int, newTask: Task) {
        taskGroupList[pos].tasks.add(newTask)
        notifyItemChanged(pos)
    }

    /*
    // ########## Deleting task entries ##########
    fun deleteTasks() {
        // Clearing entire list
        if (numSelected == taskCount) {
            // Empty everything
            taskCount = 0
            taskGroupList.clear()
            notifyDataSetChanged()

            // Reset min date and clear all selected
            minDate = baseMinDate
            numSelected = 0
            return
        }

        if (numSelected == 0)
            return

        var start = 0
        var end: Int = taskList.size - 1

        // Loop until all selected tasks are removed
        while (numSelected != 0) {
            // 1st loop = from 0 to tasList last index
            for (index in start..end) {
                // Task to delete found
                if (taskList[index].selected) {

                    // [1]. If it has a visible date (Deleting top-most task with date attached)
                    if (!taskList[index].hideDate) {
                        // Check below to see if there's another task with the same date
                        if (index != end && taskList[index + 1].hideDate) {
                            // If there is, toggle the one below to show its date. If not do nothing
                            taskList[index + 1].hideDate = false
                            notifyItemChanged(index + 1)
                        }
                    }

                    // [2]. If task is the min date (topmost task), update min date to next one
                    if (index == 0) {
                        // [A]. It is the only task in the list, reset to default value
                        if (taskList.size == 1)
                            minDate = baseMinDate

                        // [B]. If next task is a differing date, assign it to new min date
                        else if (taskList[index + 1].id != minDate)
                            minDate = taskList[index + 1].id
                    }

                    // Deselect it then remove it
                    taskList[index].selected = false
                    taskList.removeAt(index)
                    notifyItemRemoved(index)

                    // Update starting index to be current index (so we skip already visited tasks)
                    start = index
                    end--

                    // Reduce count left to delete and then reset
                    numSelected--
                    break
                }
            }
        }
    }

    fun selectAll() {
        for (index in 0 until taskList.size) {
            taskList[index].selected = true
            notifyItemChanged(index)
        }

        numSelected = taskList.size
    }
    */

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
            taskAdapter = AdapterTasks(taskGroup.tasks, clickListener)
            // Assign layout manager + adapter
            tasksRV.apply {
                layoutManager = LinearLayoutManager(tasksRV.context, RecyclerView.VERTICAL, false)
                adapter = taskAdapter
            }
        }
    }
}
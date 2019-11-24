package com.example.myapplication

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

// Add Item tutorial: https://blog.stylingandroid.com/recyclerview-animations-add-remove-items/
// ? ignore if null return/match

// OnClick: https://stackoverflow.com/questions/54219825/android-kotlin-how-to-add-click-listener-to-recyclerview-adapter

// Unit == equivalent to void return type in Java

class AdapterTasks(private val taskList: ArrayList<Task>) : RecyclerView.Adapter<ViewHolderTasks>() {

    // Selected tasks (mark as complete or delete)
    var numSelected:Int = 0

    // Used for sorting, default value ensures new min value is always replaced with first entry
    val baseMinDate:Int = 90000000
    var minDate:Int = baseMinDate

    // Number of items in table view
    override fun getItemCount(): Int { return taskList.size }

    // Creating cell
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderTasks {
        // Use inflate function found in Util
        val inflatedView = parent.inflate(R.layout.row_tasklist, false)

        // Return viewholder containing cell layout and clickListener
        return ViewHolderTasks(inflatedView, clickListener)
    }

    // When cell made
    override fun onBindViewHolder(holder: ViewHolderTasks, position: Int) {
        // Assign description and date to task based on stored array
        holder.bind(taskList[position])
    }

    // ########## Button callable functions ##########
    fun addTask(new: Task) {
        // ---------- Auto Sorting Entries ----------
        // [A]. Check for earliest date (Case [1] and [2] will never match if new.date < minDate)
        if (minDate > new.id) {
            // New date is earlier, make it the new min date and insert at the top
            minDate = new.id
            insert(new, 0)
            return
        }

        // [B]. Otherwise start from latest entry and move upwards
        for (pos in taskList.lastIndex downTo 0 step 1) {
            // [1]. Matching date, append at end of matching position and hide date
            if (taskList[pos].id == new.id) {
                new.hideDate = true
                insert(new, pos + 1)
                return
            }
            // [2]. Date reached is earlier, insert new entry after it
            if (taskList[pos].id < new.id) {
                insert(new, pos + 1)
                return
            }
        }
    }

    fun deleteTasks() {
        // Clearing entire list
        if (numSelected == taskList.size) {
            // Empty entire task list
            taskList.clear()
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

    fun toggleTask(position: Int) :Int {
        // Get referenced task item
        val task:Task = taskList[position]

        // Switch its state to the opposite (selected/deselected)
        task.selected = !task.selected
        notifyItemChanged(position)

        // Increment/Decrement internal counts
        val isSelected = task.selected
        if (isSelected) { numSelected++ } else { numSelected-- }

        return numSelected
    }

    fun selectAll() {
        for (index in 0 until taskList.size) {
            taskList[index].selected = true
            notifyItemChanged(index)
        }

        numSelected = taskList.size
    }

    // ########## onClick functionality/variables ##########
    lateinit var clickListener: ClickListener
    fun setOnItemClickListener(newCL: ClickListener) { clickListener = newCL }
    interface ClickListener { fun onClick(pos: Int, aView: View) }

    // ########## Internal functions ##########
    private fun insert(task: Task, pos: Int = taskList.size) {
        taskList.add(pos, task)
        notifyItemInserted(pos)
    }
}
package com.example.myapplication

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.tasklist_row.view.*

// Add Item tutorial: https://blog.stylingandroid.com/recyclerview-animations-add-remove-items/
// ? ignore if null return/match

// OnClick tutorial: https://www.andreasjakl.com/recyclerview-kotlin-style-click-listener-android/

class AdapterTasks(
    private val taskList: ArrayList<Task>) :
    RecyclerView.Adapter<AdapterTasks.ViewHolder>()
{
    // Selected tasks (mark as complete or delete
    var numSelected:Int = 0

    // Used for sorting, default value ensures new min value is always replaced with first entry
    val baseMinDate:Int = 90000000
    var minDate:Int = baseMinDate

    // Number of items in table view
    override fun getItemCount(): Int {
        return taskList.size
    }

    // Creating cell
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Use parent parameter for layout inflater, create cell via main_row layout file
        val layoutInflater = LayoutInflater.from(parent?.context)
        val row = layoutInflater.inflate(R.layout.tasklist_row, parent, false)

        // Return wiewholder containing cell layout
        return ViewHolder(row)
    }

    // When cell made
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Assign description and date to task based on stored array
        val taskItem = taskList[position]
        holder.bindTask(taskItem)
    }

    // ########## Required Viewholder class for Adapter ##########
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        // Defining reference to task description text in layout
        private val taskDesc = itemView.taskDesc
        private val taskDate = itemView.taskDate
        private val date = itemView.date

        private val checkBox = itemView.taskToggle

        fun bindTask(newTask: Task) {
            taskDesc.text = newTask.desc
            taskDate.text = newTask.date

            // Toggle date visibility
            showHideDate(newTask.hideDate)

            // Toggle checkbox depending whether previously selected
            checkBox.isChecked = newTask.selected

            // Setup toggle functionality
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                // Update number selected if values are different (manual check vs internal update)
                if (newTask.selected != isChecked) {
                    newTask.selected = isChecked

                    // Increment when selected and decrement when de-selected
                    if (isChecked)
                        numSelected++
                    else
                        numSelected--
                }
            }
        }

        private fun showHideDate(isHidden: Boolean){
            if (isHidden)
                date.visibility = View.INVISIBLE
            else
                date.visibility = View.VISIBLE
        }
    }

    // ########## Extra functions ##########
    fun addItem(new: Task) {
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

    fun deleteItems() {
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
                        if (taskList[index + 1].id != minDate)
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

    fun clearList() {
        // Empty entire task list
        taskList.clear()
        notifyDataSetChanged()

        // Reset min date and clear all selected
        minDate = baseMinDate
        numSelected = 0
    }

    private fun insert(task: Task, pos: Int = taskList.size) {
        taskList.add(pos, task)
        notifyItemInserted(pos)
    }
}
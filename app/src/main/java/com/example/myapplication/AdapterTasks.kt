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

            showHideDate(newTask.hideDate)
            toggleSelected(newTask.selected)

            // Setup toggle functionality
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                // Update only when state has changed and internally value needs to match
                if (newTask.selected != isChecked) {
                    newTask.selected = isChecked

                    if (isChecked)
                        numSelected++
                    else
                        numSelected--

                    Log.d("Test", "Selected: $numSelected")
                }
            }
        }

        private fun showHideDate(isHidden: Boolean){
            if (isHidden)
                date.visibility = View.INVISIBLE
            else
                date.visibility = View.VISIBLE
        }

        private fun toggleSelected(isSelected: Boolean) {
            checkBox.isChecked = isSelected
            Log.d("Test", "Selected: $numSelected")
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

        var iterator = taskList.iterator()
        while (iterator.hasNext()) {
            val task:Task = iterator.next()

            if (task.selected) {
                numSelected--
                task.selected = false
                iterator.remove()
            }

            if (numSelected == 0)
                break
        }

        notifyDataSetChanged()

    }

    fun clearList() {
        // Empty entire task list
        taskList.clear()
        notifyDataSetChanged()
        // Reset min date
        minDate = baseMinDate
    }

    private fun insert(task: Task, pos: Int = taskList.size) {
        taskList.add(pos, task)
        notifyItemInserted(pos)
    }
}

/**
 * Ideas:
 * 
 */
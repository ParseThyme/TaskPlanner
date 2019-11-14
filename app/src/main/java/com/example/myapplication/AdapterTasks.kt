package com.example.myapplication

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.tasklist_row.view.*

// Add Item tutorial: https://blog.stylingandroid.com/recyclerview-animations-add-remove-items/
// ? ignore if null return/match

class AdapterTasks(private val taskList: ArrayList<Task>) : RecyclerView.Adapter<AdapterTasks.ViewHolder>() {
    // Dates that tasks have been assigned to
    var dates = ArrayList<Int>()

    // Used for sorting, default value ensures new min value is always replaced with first entry
    var minDate:Int = 90000000

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

    // Required Viewholder class for Adapter
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // Defining reference to task description text in layout
        private val taskDesc = itemView.taskDesc
        private val dateChosen = itemView.dateChosen
        private val dateCard = itemView.dateCard

        fun bindTask(newTask: Task) {
            taskDesc.text = newTask.desc
            dateChosen.text = newTask.date

            toggleDateVisibility(newTask.hideDate)
        }

        private fun toggleDateVisibility(isHidden: Boolean){
            if (isHidden)
                dateCard.visibility = View.INVISIBLE
            else
                dateCard.visibility = View.VISIBLE
        }
    }

    // ########## Extra functions ##########
    fun addItem(new: Task) {
        // ---------- Auto Sorting Entries ----------
        // [A]. Check for earliest date (Case [1] and [2] will never match if new.date < minDate)
        if (new.id < minDate) {
            // New date is earlier, make it the new min date and insert at the top
            minDate = new.id
            insert(new, 0)
            return
        }

        // [B]. Otherwise start from latest entry and move upwards
        for (pos in taskList.lastIndex downTo 0 step 1) {
            // [1]. Matching date, append at end of matching position and hide date
            if (new.id == taskList[pos].id) {
                new.hideDate = true
                insert(new, pos + 1)
                return
            }
            // [2]. New date is later, insert after date that precedes it
            if (new.id > taskList[pos].id) {
                insert(new, pos + 1)
                return
            }
        }
    }

    private fun insert(task: Task, pos: Int = taskList.size) {
        taskList.add(pos, task)
        notifyItemInserted(pos)
    }
}
package com.example.myapplication

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.tasklist_row.view.*

// Add Item tutorial: https://blog.stylingandroid.com/recyclerview-animations-add-remove-items/
// ? ignore if null return/match

class AdapterTasks(private val taskList: ArrayList<Task>) : RecyclerView.Adapter<AdapterTasks.ViewHolder>() {
    // Dates that tasks have been assigned to
    var dates = ArrayList<Int>()

    // Used for sorting when adding in new entries
    var latestEntry:Int = 0

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

    // Required viewholder class for Adapter
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // Defining reference to task description text in layout
        private val taskDesc = itemView.taskDesc
        private val dateChosen = itemView.dateChosen
        private val dateCard = itemView.dateCard

        fun bindTask(newTask: Task) {
            taskDesc.text = newTask.desc
            dateChosen.text = newTask.date


            if (newTask.hideDate)
                toggleDateVisibility(false)
        }

        private fun toggleDateVisibility(isVisible: Boolean){
            /*
            if (isVisible)
                dateCard.visibility = View.VISIBLE
            else
                dateCard.visibility = View.INVISIBLE
             */
            if (isVisible)
                dateCard.visibility = View.VISIBLE
            else
                dateChosen.text = "-"
        }
    }

    // ########## Extra functions ##########
    fun addItem(newTask: Task) {
        // ---------- Auto Sorting Entries ----------
        // [1]. Task added is later date than latest entry, add to end
        if (newTask.id > latestEntry) {
            insert(newTask)
            latestEntry = newTask.id
            return
        }

        // [2]. Task is same as latest entry, add to end and hide date
        if (newTask.id == latestEntry) {
            newTask.hideDate = true
            insert(newTask)
            return
        }

        // [3]. Task added is an earlier date than the latest entry
        if (newTask.id < latestEntry) {
            // Start from end and go upwards to find position to insert it in
            for (pos in taskList.lastIndex downTo 0 step 1) {
                // Reached index where existing date found, follow logic in [2]
                if (taskList[pos].id == newTask.id) {
                    newTask.hideDate = true
                    insert(newTask, pos + 1)
                    return
                }
                // Reached index where date is earlier than inserted date, insert new date after
                if (taskList[pos].id < newTask.id) {
                    insert(newTask, pos + 1)
                    return
                }
            }
        }
    }

    private fun insert(task: Task, pos: Int = taskList.size) {
        taskList.add(pos, task)
        notifyItemInserted(pos)
    }
}
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

class AdapterTasks(private val taskList: ArrayList<Task>) : RecyclerView.Adapter<AdapterTasks.ViewHolder>() {
    // Dates that tasks have been assigned to
    var dates = ArrayList<String>()

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
        holder.bindTask(taskItem.desc, taskItem.date)
    }

    // Required viewholder class for Adapter
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // Defining reference to task description text in layout
        private val taskDesc = itemView.taskDesc
        private val dateChosen = itemView.dateChosen
        private val dateCard = itemView.dateCard

        fun bindTask(desc:String, date:String) {
            taskDesc.text = desc
            dateChosen.text = date
        }

        fun hideDate(){
            dateCard.visibility = View.GONE
        }
    }

    // ########## Extra functions ##########
    fun addItem(newTask: Task) {
        taskList.add(newTask)
        notifyItemInserted(taskList.size)

        taskList.sortBy { it.id }
        notifyDataSetChanged()
    }
}
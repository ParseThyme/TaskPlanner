package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Add Item tutorial: https://blog.stylingandroid.com/recyclerview-animations-add-remove-items/

class AdapterTasks(val taskList: ArrayList<Task>) : RecyclerView.Adapter<AdapterTasks.ViewHolder>() {
    // Number of items in table view
    override fun getItemCount(): Int {
        return taskList.size
    }

    // Creating cell
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Use parent parameter for layout inflater, create cell via main_row layout file
        val layoutInflater = LayoutInflater.from(parent?.context)
        val row = layoutInflater.inflate(R.layout.tasklist_row, parent, false)

        // Return Viewholder containing cell layout
        return ViewHolder(row)
    }

    // When cell made
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Assign description and date to task based on stored array
        holder?.taskDesc?.text = taskList[position].desc
        holder?.dateChosen?.text = taskList[position].date
    }

    // Required viewholder class for Adapter
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // Defining reference to task description text in layout
        val taskDesc = itemView.findViewById<TextView>(R.id.taskDesc)
        val dateChosen = itemView.findViewById<TextView>(R.id.dateChosen)

        // Add underline to dateChosen
    }

    // ########## Extra functions ##########
    fun addItem(newTask: Task) {
        taskList.add(newTask)
        notifyItemInserted(taskList.lastIndex)
    }

}
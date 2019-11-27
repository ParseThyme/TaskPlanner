package com.example.myapplication

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rv_taskentry.view.*

class AdapterTasks(private val taskList : List<Task>): RecyclerView.Adapter<AdapterTasks.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =  parent.inflate(R.layout.rv_taskentry, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int { return taskList.size }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(taskList[position])
    }

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        private val desc = itemView.desc
        private val selectedIcon = itemView.selected

        fun bind(task: Task) {
            desc.text = task.desc

            // Toggle selected icon
            toggleSelected(task.selected)
        }

        // ########## Toggling functionality ##########
        private fun toggleSelected(isSelected: Boolean) {
            if (isSelected)
                selectedIcon.visibility = View.VISIBLE
            else
                selectedIcon.visibility = View.GONE
        }
    }
}

// ########## Data Type ##########
data class Task (
    val desc : String = "",
    //val time : String = ""

    val selected : Boolean = false
)
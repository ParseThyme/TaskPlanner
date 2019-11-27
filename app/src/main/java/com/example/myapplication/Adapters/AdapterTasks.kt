package com.example.myapplication.Adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Interfaces.ItemClickListener
import com.example.myapplication.R
import com.example.myapplication.inflate
import kotlinx.android.synthetic.main.rv_taskentry.view.*

class AdapterTasks(private val taskList : List<Task>): RecyclerView.Adapter<AdapterTasks.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =  parent.inflate(R.layout.rv_taskentry, false)
        return ViewHolder(v, clickListener)
    }

    override fun getItemCount(): Int { return taskList.size }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(taskList[position])
    }

    fun toggleTask(position: Int) : Boolean{
        // Get referenced task item
        val task:Task = taskList[position]

        // Switch its state to the opposite (selected/deselected)
        task.selected = !task.selected
        notifyItemChanged(position)

        return task.selected
    }

    // ########## ViewHolder ##########
    inner class ViewHolder(itemView : View, private val clickListener: ItemClickListener)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {
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

        // ########## onClick functionality/variables ##########
        init { itemView.setOnClickListener(this) }
        override fun onClick(v: View) { clickListener.onClick(adapterPosition, v) }
    }

    // ########## onClick functionality/variables ##########
    private lateinit var clickListener: ItemClickListener
    fun setOnItemClickListener(newCL: ItemClickListener) { clickListener = newCL }
}

// ########## Data Type ##########
data class Task (
    val desc : String = "",
    //val time : String = ""

    var selected : Boolean = false
)
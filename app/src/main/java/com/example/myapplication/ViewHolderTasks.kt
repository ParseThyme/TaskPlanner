package com.example.myapplication

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.AdapterTasks.*
import kotlinx.android.synthetic.main.row_tasklist.view.*

// OnClick tutorial: https://stackoverflow.com/questions/54219825/android-kotlin-how-to-add-click-listener-to-recyclerview-adapter

class ViewHolderTasks(view: View, private val clickListener: ClickListener):
    RecyclerView.ViewHolder(view), View.OnClickListener
{
    // Defining reference to task description text in layout
    private val taskDesc = view.taskDesc
    private val taskDate = view.taskDate
    private val date = view.date
    private val selectedIcon = view.selected

    fun bind(newTask: Task) {
        taskDesc.text = newTask.desc
        taskDate.text = newTask.date

        // Toggle date visibility
        showHideDate(newTask.hideDate)
        toggleSelected(newTask.selected)

        /*
        // Toggle checkbox depending whether previously selected
        checkBox.isChecked = newTask.selected

        // Setup toggle functionality
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            // Update number selected if values are different (manual check vs internal update)
            if (newTask.selected != isChecked) {
                newTask.selected = isChecked

                /*
                // Increment when selected and decrement when de-selected
                if (isChecked)
                    numSelected++
                else
                    numSelected--
                 */

                // Increment when selected and decrement when de-selected
                if (isChecked) {
                    numSelected++
                    background.setBackgroundColor(Color.GREEN)
                }
                else {
                    numSelected--
                    background.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }*/
    }

    // ########## onClick functionality/variables ##########
    init { view.setOnClickListener(this) }
    override fun onClick(v: View) { clickListener.onClick(adapterPosition, v) }

    // ########## Toggling functionality ##########
    private fun showHideDate(isHidden: Boolean){
        if (isHidden)
            date.visibility = View.INVISIBLE
        else
            date.visibility = View.VISIBLE
    }

    private fun toggleSelected(isSelected: Boolean) {
        if (isSelected)
            selectedIcon.visibility = View.VISIBLE
        else
            selectedIcon.visibility = View.GONE
    }
}
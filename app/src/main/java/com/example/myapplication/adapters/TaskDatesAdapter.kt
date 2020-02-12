package com.example.myapplication.adapters

import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.*
import com.example.myapplication.data_classes.TaskDate
import kotlinx.android.synthetic.main.task_date_rv.view.*

class TaskDatesAdapter(private val taskDates: ArrayList<TaskDate>,
                       private val settings: Settings,
                       private val today: CardView)
    : RecyclerView.Adapter<TaskDatesAdapter.ViewHolder>()
{
    var selected: Int = -1
    private set

    override fun getItemCount(): Int { return taskDates.size }
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) { holder.bind(taskDates[pos]) }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Use inflate function found in Util then return containing cell layout and clickListener
        val inflatedView = parent.inflate(R.layout.task_date_rv, false)
        return ViewHolder(inflatedView)
    }

    fun clearCurrentlySelected() {
        val current:Int = selected
        // Change selected to be today's date
        selected = -1
        // Notify viewHolder to unhighlight previously selected
        notifyItemChanged(current)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(entry: TaskDate) {
            // Assign date entry
            itemView.txtDate.text = entry.dateShortest

            // Set checked if selected and update background color
            if (selected == adapterPosition)
                itemView.cardDate.applyBackgroundColor(settings.highlightColor)
            else
                itemView.cardDate.applyBackgroundColor(Color.WHITE)

            // Update this to be selected on click
            itemView.cardDate.setOnClickListener {
                // Store reference to previously selected
                val previous: Int = selected

                // Previously selected was today's date, clear its selection
                if (previous == -1) today.applyBackgroundColor(Color.WHITE)

                // Update only if previous is not the same as current
                // E.g. Clicking on the same already selected date should do nothing
                if (previous != adapterPosition) {
                    // Highlight currently selected
                    selected = adapterPosition
                    notifyItemChanged(selected)

                    // Unhighlight previously selected if its in the viewHolder (exclude today's date)
                    if (previous >= 0)
                        notifyItemChanged(previous)
                    else
                        today.applyBackgroundColor(Color.WHITE)
                }
            }
        }
    }
}

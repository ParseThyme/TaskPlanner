package com.example.myapplication.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data_classes.TaskTag
import com.example.myapplication.inflate
import kotlinx.android.synthetic.main.popup_tag_entry.view.*

class TaskTagAdapter(private val tagsList: ArrayList<TaskTag>,
                     private val selectAndClose: (TaskTag) -> Unit)
    : RecyclerView.Adapter<TaskTagAdapter.ViewHolder>()
{

    override fun getItemCount(): Int { return tagsList.size }
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) { holder.bind(tagsList[pos]) }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.popup_tag_entry))
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(entry: TaskTag) {
            itemView.iconTag.apply {
                setImageResource(entry.icon)                    // Set icon to match taskTag
                setOnClickListener { selectAndClose(entry) }    // When tag clicked, close popup window
            }
        }
    }
}
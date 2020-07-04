package com.example.myapplication.data_classes

import android.view.View
import android.widget.ImageView
import com.example.myapplication.R
import kotlinx.android.synthetic.main.task_entry_rv.view.*

// ########## Data Type ##########
data class Task (
    var desc : String = "",

    var tag: Int = R.drawable.tag_base,
    var time: TaskTime = TaskTime(0,0,TimeOfDay.PM, 0),

    var selected : Boolean = false
)

fun ImageView.assignTag(tag: Int) {
    when (tag == R.drawable.tag_base) {
        // No tag, don't display anything
        true -> this.visibility = View.INVISIBLE
        // Otherwise: get resource, set tag accordingly and display
        false -> {
            this.setImageResource(tag)
            this.visibility = View.VISIBLE
        }
    }
}
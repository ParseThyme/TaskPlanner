package com.example.myapplication.data_classes

import android.widget.ImageView
import com.example.myapplication.R

// ########## Data Type ##########
data class Task (
    var desc : String = "",

    var tag: TaskTag = TaskTag.NONE,
    var time: TaskTime = TaskTime(0,0,"AM", 0),

    var selected : Boolean = false
)

enum class TaskTag { NONE, BOOKING, BUY, EVENT }

fun ImageView.setImageResourceFromTag(tag: TaskTag) {
    val imageResource = when (tag) {
        TaskTag.BOOKING -> R.drawable.ic_tag_booking
        TaskTag.BUY -> R.drawable.ic_tag_buy
        TaskTag.EVENT -> R.drawable.ic_tag_event
        else -> R.drawable.ic_tag_base
    }

    // Assign image resource and internal tag (for converting to Tag enum later)
    this.setImageResource(imageResource)
    this.tag = tag
}

// https://www.programiz.com/kotlin-programming/examples/enum-by-string-value
fun ImageView.getTagFromImageResource(): TaskTag {
    // Return no tag for unassigned image resources
    if (this.tag == null) { return TaskTag.NONE }
    // Get string tag from imageResource
    val tagString: String = this.tag.toString()
    // Convert it to Tag enum
    return TaskTag.valueOf(tagString)
}
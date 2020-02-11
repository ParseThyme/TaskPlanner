package com.example.myapplication.data_classes

import android.util.Log
import android.widget.ImageView
import com.example.myapplication.R

// ########## Data Type ##########
data class Task (
    var desc : String = "",

    var tag: Tag = Tag.NONE,
    var time: TaskTime = TaskTime(0,0,"AM", 0),

    var selected : Boolean = false
)

enum class Tag { NONE, BOOKING, BUY, EVENT }

fun ImageView.setImageResourceFromTag(tag: Tag) {
    val imageResource = when (tag) {
        Tag.BOOKING -> R.drawable.ic_tag_booking
        Tag.BUY -> R.drawable.ic_tag_buy
        Tag.EVENT -> R.drawable.ic_tag_event
        else -> R.drawable.ic_tag_base
    }

    // Assign image resource and internal tag (for converting to Tag enum later)
    this.setImageResource(imageResource)
    this.tag = tag
}

// https://www.programiz.com/kotlin-programming/examples/enum-by-string-value
fun ImageView.getTagFromImageResource(): Tag {
    // Return no tag for unassigned image resources
    if (this.tag == null) { return Tag.NONE }
    // Get string tag from imageResource
    val tagString: String = this.tag.toString()
    // Convert it to Tag enum
    return Tag.valueOf(tagString)
}
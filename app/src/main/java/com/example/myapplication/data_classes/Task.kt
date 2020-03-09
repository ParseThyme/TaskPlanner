package com.example.myapplication.data_classes

import android.widget.ImageView
import com.example.myapplication.R

// ########## Data Type ##########
data class Task (
    var desc : String = "",

    var tag: Int = R.drawable.tag_base,
    var time: TaskTime = TaskTime(0,0,"AM", 0),

    var selected : Boolean = false
)

/*
enum class TaskTagOld { NONE, BOOKING, BUY, EVENT }

fun ImageView.setImageResourceFromTag(tagOld: TaskTagOld) {
    val imageResource = when (tagOld) {
        TaskTagOld.BOOKING -> R.drawable.tag_booking
        TaskTagOld.BUY -> R.drawable.tag_buy
        TaskTagOld.EVENT -> R.drawable.tag_event
        else -> R.drawable.tag_base
    }

    // Assign image resource and internal tag (for converting to Tag enum later)
    this.setImageResource(imageResource)
    this.tag = tagOld
}
*/
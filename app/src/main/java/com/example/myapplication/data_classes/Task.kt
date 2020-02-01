package com.example.myapplication.data_classes

import android.widget.ImageView
import com.example.myapplication.R

// ########## Data Type ##########
data class Task (
    var desc : String = "",

    var tag: Tag = Tag.NONE,
    var timeStart: TaskTime = TaskTime("0","0","AM"),
    var timeEnd: TaskTime = TaskTime("0","0","AM"),

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

    this.setImageResource(imageResource)
}
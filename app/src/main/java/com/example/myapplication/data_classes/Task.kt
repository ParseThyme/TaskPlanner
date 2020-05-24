package com.example.myapplication.data_classes

import com.example.myapplication.R
// ########## Data Type ##########
data class Task (
    var desc : String = "",

    var tag: Int = R.drawable.tag_base,
    var time: TaskTime = TaskTime(0,0,TimeOfDay.PM, 0),

    var selected : Boolean = false
)

fun Task.clear(paramType: TaskParam) {
    when (paramType) {
        TaskParam.Tag -> this.tag = R.drawable.tag_base
        TaskParam.Time -> this.time.clear()
    }
}
enum class TaskParam { Tag, Time }
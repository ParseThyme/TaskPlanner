package com.example.myapplication.save_data

import com.example.myapplication.data_classes.Fold
import com.example.myapplication.data_classes.Task
import com.example.myapplication.data_classes.TaskDate

data class TaskGroup (
    // A. Assigned as standard group
    val date: TaskDate = TaskDate(),                // Date category of group
    val taskList: ArrayList<Task> = arrayListOf(),  // Child task list

    var numSelected: Int = 0,                       // Child tasks selected
    var state: Fold = Fold.OUT                      // Toggle state (expanded/collapsed)
)
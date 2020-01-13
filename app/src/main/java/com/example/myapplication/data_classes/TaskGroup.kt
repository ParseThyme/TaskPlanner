package com.example.myapplication.data_classes

// ########## Data Type ##########
data class TaskGroup (
    val date: String = "",
    val taskList: ArrayList<Task> = arrayListOf(),

    // Identification
    val id: Int = 0,

    // When tasks selected
    var numSelected: Int = 0,
    // Toggle state (expanded/collapsed)
    var state: ViewState = ViewState.EXPANDED
)

fun TaskGroup.allSelected() : Boolean {
    if (this.numSelected == this.taskList.size)
        return true

    return false
}

fun TaskGroup.toggleExpandCollapse(): ViewState {
    state = if (state == ViewState.EXPANDED)
        ViewState.COLLAPSED
    else
        ViewState.EXPANDED

    return state
}

fun TaskGroup.isExpanded() : Boolean {
    if (state == ViewState.EXPANDED) return true
    return false
}

enum class ViewState { EXPANDED, COLLAPSED }
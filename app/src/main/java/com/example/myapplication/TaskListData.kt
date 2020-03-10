package com.example.myapplication

class TaskListData(
    private var taskCount: Int,
    private var collapsedCount: Int,
    private var selected: Int)
{
    // Pass from mainActivity into TaskGroupAdapter
    fun updateTaskCount(delta: Int) { taskCount += delta }
    fun updateCollapsedCount(delta: Int) { collapsedCount += delta }
    fun updatedSelectedCount(delta: Int) { selected += delta }
}
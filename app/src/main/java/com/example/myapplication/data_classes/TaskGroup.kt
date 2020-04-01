package com.example.myapplication.data_classes

abstract class TaskGroup()

// Check if Header or Row
fun TaskGroup.isHeader() : Boolean { return (this is TaskGroupHeader) }

enum class RowType { HEADER, ROW }
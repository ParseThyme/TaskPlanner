package com.example.myapplication

data class TaskMonth (
    val label: String = "",
    val labelShort: String = "",
    val days: Int = 28
)

val January = TaskMonth("January", "Jan",31)
val February = TaskMonth("February", "Feb",28)
val March = TaskMonth("March", "Mar",31)
val April = TaskMonth("April", "Apr",30)
val May = TaskMonth("May", "May",31)
val June = TaskMonth("June", "Jun",30)
val July = TaskMonth("July", "Jul",31)
val August = TaskMonth("August", "Aug",31)
val September = TaskMonth("September", "Sep",30)
val October = TaskMonth("October", "Oct",31)
val November = TaskMonth("November", "Nov", 30)
val December = TaskMonth("December", "Dec", 31)

val months: HashMap<Int, TaskMonth> = hashMapOf(
    0 to January, 1 to February, 2 to March, 3 to April, 4 to May, 5 to June,
    6 to July, 7 to August, 8 to September, 9 to October, 10 to November, 11 to December
)
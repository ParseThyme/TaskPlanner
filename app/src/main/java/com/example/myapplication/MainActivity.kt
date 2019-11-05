package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

// Resource: https://www.youtube.com/watch?v=jS0buQyfJfs

class MainActivity : AppCompatActivity() {
    private val taskList = ArrayList<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Add content to task list
        taskList.add(Task("Do some Android Programming"))
        taskList.add(Task("Eat some food"))

        // Setup Manager
        taskListLayout.layoutManager = LinearLayoutManager(this)
        // Setup Adapter. Table to render out items on list
        taskListLayout.adapter = AdapterTasks(taskList)
    }
}

package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.additem_view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.days

// ########## Resources ##########
// - RecyclerView:
//      https://www.youtube.com/watch?v=jS0buQyfJfs
// - Popup Dialog box:
//      https://www.youtube.com/watch?v=2Nj6qCtaUqw
// - Disabling button/enabling based on text field
//      https://www.youtube.com/watch?v=Vy_4sZ6JVHM

// val == assigned once, not changed
// var == changeable, can be reassigned

class MainActivity : AppCompatActivity() {
    // TaskList (Center Area)
    private val taskList = ArrayList<Task>()
    private val taskAdapter = AdapterTasks(taskList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Add content to task list
        taskList.add(Task("Do some Android Programming"))
        taskList.add(Task("Eat some food"))

        // Setup Manager
        taskListLayout.layoutManager = LinearLayoutManager(this)
        // Setup Adapter. Table to render out items on list
        taskListLayout.adapter = taskAdapter

        // Setup toolbar at bottom
        setupToolbar()
    }

    private fun setupToolbar() {
        // Add new task button
        setupAddNewTask()
    }

    private fun setupAddNewTask() {
        // Add item (Open dialog box for new entry)
        buttonAdd.setOnClickListener {

            // Inflate dialog
            val addDialogView = LayoutInflater.from(this).inflate(R.layout.additem_view, null)
            // Build using alert dialog box
            val addBuilder = AlertDialog.Builder(this)
            addBuilder.setView(addDialogView)
            //addBuilder.setTitle("Add New Task")
            // Show dialog box
            val addDialogBox = addBuilder.show()

            // Input Validation:
            // 1. TextWatcher, ensure confirm button only enabled when task entered
            addDialogBox.taskDescription.addTextChangedListener(object: TextWatcher {
                // Unused
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                // Check when text is being changed
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val taskEntry = addDialogBox.taskDescription.text.toString().trim() // Remove blank spaces before/after string

                    // Toggle confirm button based on whether text is empty or not
                    addDialogBox.confirmButton.isEnabled = taskEntry.isNotEmpty()
                }
            })

            // 2. Calendar date
            // Ensure you can only select either today or future dates and allow only 1 month into future
            val days = 7
            addDialogBox.dateSelected.minDate = System.currentTimeMillis() - 1000
            addDialogBox.dateSelected.maxDate = System.currentTimeMillis() + (1000*60*60*24 * days)

            addDialogBox.dateSelected.setOnDateChangeListener {
                view, year, month, dayOfMonth ->
                // Months start from 0
                val dateSelected = StringBuilder()
                dateSelected.append(dayOfMonth).append("/").append(month + 1).append("/").append(year)
                println(dateSelected)
            }

            // Confirm button
            addDialogBox.confirmButton.setOnClickListener {
                // Close dialog box
                addDialogBox.dismiss()

                // Get task entry
                val taskDesc = addDialogBox.taskDescription.text.toString().trim()

                // Create new entry and add to task list
                val task = Task(taskDesc)
                taskAdapter.addItem(task)
            }
        }
    }
}

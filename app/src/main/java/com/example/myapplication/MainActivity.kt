package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.additem_view.*
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import kotlin.collections.ArrayList
import java.util.*

/** ########## Resources ##########
- RecyclerView:
   https://www.youtube.com/watch?v=jS0buQyfJfs
- Popup Dialog box:
   https://www.youtube.com/watch?v=2Nj6qCtaUqw
- Disabling button/enabling based on text field
   https://www.youtube.com/watch?v=Vy_4sZ6JVHM
**/

/**
 val == assigned once, fixed
 var == changeable, can be reassigned
**/

/** Equivalents:
-------------------------------------
 val textView = TextView(this)
 textView.visibility = View.VISIBLE
 textView.text = "test"
--------------------------------------
 val textView = TextView(this).apply {
    visibility = View.VISIBLE
    text = "test"
 }
--------------------------------------
**/

class MainActivity : AppCompatActivity() {
    // TaskList (Center Area)
    private val taskList = ArrayList<Task>()
    private val taskAdapter = AdapterTasks(taskList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Add content to task list:
        /*
        taskAdapter.addItem(Task("Task 1", "Sun 10 Nov"))
        taskAdapter.addItem(Task("Task 3", "Mon 11 Nov"))
        taskAdapter.addItem(Task("Task 2", "Sun 10 Nov"))
        */

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
            val addBuilder = AlertDialog.Builder(this).apply {
                setView(addDialogView)
            }
            //addBuilder.setTitle("Add New Task")
            // Show dialog box
            val addDialogBox = addBuilder.show()

            addDialogBox.confirmButton.isEnabled = true

            // Input Validation:
            // 1. TextWatcher, ensure confirm button only enabled when task entered
            /*
            addDialogBox.taskDesc.addTextChangedListener(object: TextWatcher {
                // Unused
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                // Check when text is being changed
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val taskEntry = addDialogBox.taskDesc.text.toString().trim() // Remove blank spaces before/after string

                    // Toggle confirm button based on whether text is empty or not
                    addDialogBox.confirmButton.isEnabled = taskEntry.isNotEmpty()
                }
            })
            */
            // 2. Calendar date
            // Format: SUN 10 NOV
            // Link: https://developer.android.com/reference/java/text/SimpleDateFormat
            val dateFormat = SimpleDateFormat("EEE d MMM")
            val idFormat = SimpleDateFormat("yyyyMMdd")

            // Ensure you can only select either today or future dates, customizable
            val calMaxDays = 30
            var cal = Calendar.getInstance()

            // Set initial date to be today
            var taskDate = dateFormat.format(cal.timeInMillis)
            addDialogBox.calendarView.minDate = cal.timeInMillis
            // Set id for sorting purposes
            var id = idFormat.format(cal.timeInMillis).toInt()

            // Add days to set end limit of calendar
            cal.add(Calendar.DATE, calMaxDays)
            addDialogBox.calendarView.maxDate = cal.timeInMillis

            // Update chosen date when user selects a differing date to default
            addDialogBox.calendarView.setOnDateChangeListener {
                view, year, month, day ->

                cal.set(year, month, day)
                taskDate = dateFormat.format(cal.timeInMillis)
                id = idFormat.format(cal.timeInMillis).toInt()
            }

            // Confirm button
            addDialogBox.confirmButton.setOnClickListener {
                // Close dialog box
                addDialogBox.dismiss()

                // Get task entry
                val taskDesc = addDialogBox.taskDesc.text.toString().trim()

                // Create new entry and add to task list
                val task = Task(id, taskDesc, taskDate)
                taskAdapter.addItem(task)
            }
        }
    }
}

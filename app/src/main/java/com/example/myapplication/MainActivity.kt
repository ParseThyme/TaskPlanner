package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_additem.*
import kotlinx.android.synthetic.main.view_additem.taskDesc
import java.text.SimpleDateFormat
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
    private val tasks = ArrayList<Task>()
    private val taskAdapter = AdapterTasks(tasks)

    // Debugging:
    private var validateInput = false

    // Selecting tasks
    var numSelected: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup Manager
        taskList.layoutManager = LinearLayoutManager(this)
        // Setup Adapter. Table to render out items on list
        taskList.adapter = taskAdapter

        // Add custom toolbar at top
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.title = ""

        runSetup()
    }

    // ########## Setup related functions ##########
    private fun runSetup() {
        // [1]. Click listener
        setupClickListener()

        // [2A]. Button - Add task
        setupAddNewTask()

        // [2B]. Button - Delete task(s)
        buttonDelete.setOnClickListener {
            taskAdapter.deleteTasks()
            numSelected = 0 // Every time delete called, all selections are cleared
            checkNumSelected()
        }
    }

    private fun setupClickListener() {
        taskAdapter.setOnItemClickListener(object: AdapterTasks.ClickListener {
            override fun onClick(pos: Int, aView: View) {
                numSelected = taskAdapter.toggleTask(pos)
                checkNumSelected()
            }
        })
    }

    private fun setupAddNewTask() {
        // Format for printed date + internal id
        // Link: https://developer.android.com/reference/java/text/SimpleDateFormat
        val dateFormat = SimpleDateFormat("EEE d MMM")
        val idFormat = SimpleDateFormat("yyyyMMdd")

        // Ensure you can only select either today or future dates, customizable
        val calMaxDays = 30

        // Setup calendar variable
        val cal = Calendar.getInstance()
        // Set initial date to be today, calculate min and max date
        val startDate = dateFormat.format(cal.timeInMillis)
        val startId = idFormat.format(cal.timeInMillis).toInt()
        val minDate = cal.timeInMillis
        // Add extra days to get max date
        cal.add(Calendar.DATE, calMaxDays)
        val maxDate = cal.timeInMillis

        // Add item (Open dialog box for new entry)
        buttonAdd.setOnClickListener {
            // Inflate dialog
            val addDialogView = LayoutInflater.from(this).inflate(R.layout.view_additem, null)
            // Build using alert dialog box
            val addBuilder = AlertDialog.Builder(this).apply {
                setView(addDialogView)
            }
            //addBuilder.setTitle("Add New Task")
            // Show dialog box and apply minDate and maxDate
            val addDialogBox = addBuilder.show().apply {
                calendarView.minDate = minDate
                calendarView.maxDate = maxDate
            }

            // Input Validation:
            // 1. TextWatcher, ensure confirm button only enabled when task entered
            if (validateInput) {
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
            } else
                addDialogBox.confirmButton.isEnabled = true

            // Set initial date and id matching to today
            var taskDate = startDate
            var id = startId

            // Override chosen date when user selects a differing date to default
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

                // Get task description entry and create new task entry
                val taskDesc = addDialogBox.taskDesc.text.toString().trim()
                val task = Task(id, taskDesc, taskDate)

                // Add new entry
                taskAdapter.addTask(task)
            }
        }
    }

    // ########## Internal functions ##########
    private fun checkNumSelected() {
        // Toggle depending on whether there is none selected or more than one
        if (numSelected == 0) {
            supportActionBar?.title = ""
        } else {
            supportActionBar?.title = "Selected: [$numSelected]"
        }
    }
}

/** TODO:
 * Sub-menu for functions: Clear all/Complete all
 * Display number of tasks selected on top.
 * Enable/disable delete/complete buttons (if 0 then disable, otherwise enable)
 * Functionality for "Mark Complete" button
 * Button: Select All
 **/
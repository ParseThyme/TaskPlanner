package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.view.size
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_additem.*
import kotlinx.android.synthetic.main.view_additem.desc
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList
import java.util.*

class MainActivity : AppCompatActivity() {
    // TaskList (Center Area)
    private val taskGroupList = ArrayList<TaskGroup>()
    private val taskGroupAdapter = AdapterTaskGroup(taskGroupList)

    // Debugging:
    private var validateInput = false

    // Navigation menu options
    private lateinit var taskAdd: MenuItem
    private lateinit var taskDelete: MenuItem
    private lateinit var taskComplete: MenuItem
    private lateinit var taskSelectAll: MenuItem

    // Selecting tasks
    private var numSelected: Int = 0
    private var mode: Mode = Mode.ADD

    // Navigation bars
    lateinit var toolbar: ActionBar
    val mainTitle = "My Task List"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Assign layout manager and adapter to recycler view
        dateGroupRV.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = taskGroupAdapter
        }

        // Divider between date categories
        val divider = DividerItemDecoration(dateGroupRV.context, DividerItemDecoration.VERTICAL)
        dateGroupRV.addItemDecoration(divider)

        /*
        taskGroupAdapter.addTask(2, "Fri 29 Nov", "Volunteering")
        taskGroupAdapter.addTask(0,"Wed 27 Nov", "Do some programming")
        taskGroupAdapter.addTask(1,"Thu 28 Nov", "Play some Diablo")
        taskGroupAdapter.addTask(0,"Wed 27 Nov", "Eat some food")
        */

        runSetup()
    }

    // ########## Setup related functions ##########
    private fun runSetup() {
        // Variable references
        setupLateInit()

        // [1]. Toolbar at top
        setupToolbar()

        // [2]. Click listener for task list
        setupClickListener()

        // [3]. At start only show New Task button
        setMode(Mode.ADD)

        // [4]. Set behaviour when clicking on bottom navigation toolbar
        bottomBar.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.menuAdd -> {
                    addNewTask()
                    true
                }
                R.id.menuDelete -> {
                    // taskGroupAdapter.deleteTasks()
                    numSelected = 0
                    checkNumSelected()
                    true
                }
                R.id.menuSelectAll -> {
                    // taskGroupAdapter.selectAll()
                    numSelected = dateGroupRV.size
                    checkNumSelected()
                    true
                }
                R.id.menuComplete -> {
                    true
                }
                else -> false
            }
        }
    }

    private fun setupToolbar() {
        // Add custom toolbar at top
        setSupportActionBar(findViewById(R.id.topBar))
        toolbar = supportActionBar!!
        updateTopToolbar(mainTitle)
    }

    private fun setupClickListener() {
        taskGroupAdapter.setOnItemClickListener(object: AdapterTaskGroup.ClickListener {
            override fun onClick(pos: Int, aView: View) {
                // numSelected = taskGroupAdapter.toggleTask(pos)
                checkNumSelected()
            }
        })
    }

    private fun addNewTask() {
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
            addDialogBox.desc.addTextChangedListener(object: TextWatcher {
                // Unused
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                // Check when text is being changed
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val taskEntry = addDialogBox.desc.text.toString().trim() // Remove blank spaces before/after string

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

            // Get task description entry, create task entry and add to adapter
            val taskDesc = addDialogBox.desc.text.toString().trim()
            taskGroupAdapter.addTask(id, taskDate, taskDesc)
        }
    }

    private fun setupLateInit() {
        taskAdd = bottomBar.menu.findItem(R.id.menuAdd)
        taskDelete = bottomBar.menu.findItem(R.id.menuDelete)
        taskComplete = bottomBar.menu.findItem(R.id.menuComplete)
        taskSelectAll = bottomBar.menu.findItem(R.id.menuSelectAll)
    }

    // ########## Change values/display ##########
    private fun setMode(newMode: Mode) {
        when (newMode) {
            Mode.ADD -> {
                taskAdd.isVisible = true

                taskDelete.isVisible = false
                taskComplete.isVisible = false
                taskSelectAll.isVisible = false
            }
            Mode.SELECTION -> {
                taskDelete.isVisible = true
                taskComplete.isVisible = true
                taskSelectAll.isVisible = true

                taskAdd.isVisible = false
            }
        }
    }

    private fun updateTopToolbar(newTitle: String) {
        toolbar.title = newTitle
    }

    // ########## Internal functions ##########
    private fun checkNumSelected() {
        // Return to add mode
        if (numSelected == 0) {
            updateTopToolbar(mainTitle)
            setMode(Mode.ADD)
        } else {
            // Entering select mode for first time
            if (mode == Mode.ADD) { setMode(Mode.SELECTION) }

            // Display number selected
            updateTopToolbar("Selected: [$numSelected]")
        }
    }
}

enum class Mode { ADD, SELECTION }
package com.example.myapplication

import com.example.myapplication.adapters.AdapterTaskGroup
import com.example.myapplication.data_classes.Task
import com.example.myapplication.data_classes.TaskGroup

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.view.size
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast

import java.text.SimpleDateFormat
import java.util.*

import kotlin.collections.ArrayList

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_additem.*
import kotlinx.android.synthetic.main.view_additem.desc

class MainActivity : AppCompatActivity() {
    // TaskList (Center Area)
    private val taskGroupList = ArrayList<TaskGroup>()
    private val taskClickedFunction = { position : Int, task : Task -> taskClicked(position, task) }
    private val taskGroupAdapter = AdapterTaskGroup(taskGroupList, taskClickedFunction)

    // Debugging:
    private var validateInput = false

    // Navigation menu options
    private lateinit var taskAdd: MenuItem
    private lateinit var taskDelete: MenuItem
    private lateinit var taskComplete: MenuItem
    private lateinit var taskSelectAll: MenuItem

    // Selecting tasks
    private var allSelected = false
    private var taskCount: Int = 0
    private var selected: Int = 0
    private var mode: Mode = Mode.ADD

    // Navigation bars
    private lateinit var toolbar: ActionBar
    private val mainTitle = "My Task List"

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
        taskGroupAdapter.addTask(1,"Thu 28 Nov", "Play some Diablo III")
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

        // [2]. At start only show New Task button
        setMode(Mode.ADD)

        // [3]. Set behaviour when clicking on bottom navigation toolbar
        bottomBar.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.menuAdd -> {
                    addNewTask()
                    true
                }
                R.id.menuDelete -> {
                    // Check if deleting all or deleting specific amount
                    if (selected == taskCount) {
                        taskGroupAdapter.deleteTasks(0, true)
                        taskCount = 0
                    } else {
                        taskGroupAdapter.deleteTasks(selected)
                        taskCount -= selected
                    }

                    // Clear selections and return to add mode
                    setMode(Mode.ADD)
                    selected = 0
                    true
                }
                R.id.menuSelectAll -> {
                    if (selected != taskCount) {
                        taskGroupAdapter.toggleSelectAll()
                        selected = taskCount
                        updateTopToolbar("Selected: [$selected]")
                    }
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

            taskCount++
        }
    }

    private fun setupLateInit() {
        taskAdd = bottomBar.menu.findItem(R.id.menuAdd)
        taskDelete = bottomBar.menu.findItem(R.id.menuDelete)
        taskComplete = bottomBar.menu.findItem(R.id.menuComplete)
        taskSelectAll = bottomBar.menu.findItem(R.id.menuSelectAll)
    }

    // ########## OnClick ##########
    private fun taskClicked (position: Int, task: Task) {
        // Update counts based on whether task selected/deselected
        if (task.selected) {
            selected++

            // Selected first task, change to selection mode
            if (selected == 1)
                setMode(Mode.SELECTION)
        }
        else {
            selected--

            // Deselect last task, return to add mode
            if (selected == 0) {
                setMode(Mode.ADD)
                updateTopToolbar(mainTitle)
                return
            }
        }

        // Update toolbar value print for any values above 0
        updateTopToolbar("Selected: [$selected]")
    }

    // ########## Change values/display ##########
    private fun setMode(newMode: Mode) {
        mode = newMode
        when (newMode) {
            Mode.ADD -> {
                updateTopToolbar(mainTitle)

                taskDelete.isVisible = false
                taskComplete.isVisible = false
                taskSelectAll.isVisible = false

                taskAdd.isVisible = true
            }
            Mode.SELECTION -> {
                updateTopToolbar("Selected: [$selected]")

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
}

enum class Mode { ADD, SELECTION }
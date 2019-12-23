package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.AdapterTaskGroup
import com.example.myapplication.data_classes.Task
import com.example.myapplication.data_classes.TaskGroup
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.additem_view.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    // TaskList (Center Area)
    private var taskGroupList = ArrayList<TaskGroup>()
    private val taskClickedFn = { task : Task -> taskClicked(task) }
    private val dateClickedFn = { group: Int -> dateClicked(group) }
    private lateinit var taskGroupAdapter: AdapterTaskGroup

    // Selecting tasks
    private var taskCount: Int = 0
    private var selected: Int = 0
    private var mode: Mode = Mode.ADD

    // Navigation TopBar
    private lateinit var toolbar: ActionBar
    // Navigation bottom menu options
    private lateinit var taskAdd: MenuItem
    private lateinit var taskDelete: MenuItem
    private lateinit var taskComplete: MenuItem
    private lateinit var taskSelectAll: MenuItem

    // Ensure you can only select either today or future dates, ToDo: Customizable
    private val calMaxDays = 30
    // Calendar limits + starting values
    private var startDate: String = ""
    private var startId: Int = 0
    private var minDate: Long = 0
    private var maxDate: Long = 0

    // Saved/Loaded data using SharedPreferences
    private lateinit var saveLoad: SaveLoad
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Check for existing saved data, attempt to load it then create the adapter
        loadSave()

        // Assign layout manager and adapter to recycler view
        dateGroupRV.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = taskGroupAdapter
        }

        // Divider between date categories
        val divider = DividerItemDecoration(dateGroupRV.context, DividerItemDecoration.VERTICAL)
        dateGroupRV.addItemDecoration(divider)

        runSetup()
    }

    // ########## Setup related functions ##########
    private fun runSetup() {
        // Initialize variable references
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
                    taskGroupAdapter.deleteTasks(selected)
                    taskCount -= selected

                    // Clear selections and return to add mode
                    setMode(Mode.ADD)
                    selected = 0
                    updateSave()
                    true
                }
                R.id.menuSelectAll -> {
                    if (selected != taskGroupAdapter.taskCount) {
                        taskGroupAdapter.toggleAll()
                        selected = taskGroupAdapter.taskCount
                        updateSelectedCountDisplay()
                    }
                    true
                }
                R.id.menuComplete -> {
                    // ToDo
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
        // Setup new calendar instance
        val cal = Calendar.getInstance()

        // Add item (Open dialog box for new entry)
        // 1. Inflate dialog
        val addDialogView = LayoutInflater.from(this).inflate(R.layout.additem_view, null)
        // 2. Build using alert dialog box
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

            updateSave()
        }
    }

    private fun setupLateInit() {
        // Bottom toolbar variables
        taskAdd = bottomBar.menu.findItem(R.id.menuAdd)
        taskDelete = bottomBar.menu.findItem(R.id.menuDelete)
        taskComplete = bottomBar.menu.findItem(R.id.menuComplete)
        taskSelectAll = bottomBar.menu.findItem(R.id.menuSelectAll)

        // Add new task variables
        val cal = Calendar.getInstance()
        startDate = dateFormat.format(cal.timeInMillis)
        startId = idFormat.format(cal.timeInMillis).toInt()
        minDate = cal.timeInMillis
        // Add extra days to get max date
        cal.add(Calendar.DATE, calMaxDays)
        maxDate = cal.timeInMillis
    }

    // ########## OnClick ##########
    private fun dateClicked(groupNum: Int) {
        val difference: Int = taskGroupAdapter.toggleGroup(groupNum)
        selected += difference

        // Entering select mode
        if (selected > 0)
            setMode(Mode.SELECTION)

        updateSelectedCountDisplay()
    }

    private fun taskClicked (task: Task) {
        // Update counts based on whether task selected/deselected
        if (task.selected) {
            selected++

            // Selected first task, change to selection mode
            if (selected == 1)
                setMode(Mode.SELECTION)
        }
        else
            selected--

        // Update toolbar value print for any values above 0
        updateSelectedCountDisplay()
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
                updateSelectedCountDisplay()
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

    private fun updateSelectedCountDisplay() {
        // No tasks selected, return to add mode
        if (selected == 0) {
            setMode(Mode.ADD)
            updateTopToolbar(mainTitle)
            return
        } else
            updateTopToolbar("Selected: $selected")
    }

    // ########## Internal functions ##########
    private fun loadSave() {
        saveLoad = SaveLoad(this)
        taskGroupList = saveLoad.loadTaskGroupList()
        taskGroupAdapter = AdapterTaskGroup(taskGroupList, taskClickedFn, dateClickedFn)
    }

    private fun updateSave() {
        saveLoad.saveTaskGroupList(taskGroupList)
    }

    /*
    private fun deleteSave() {
        saveLoad.clearAllData()
    }
    */
}

enum class Mode { ADD, SELECTION }
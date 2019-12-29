package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Spinner
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
    // Settings
    private val settings: Settings = Settings()

    // TaskList (Center Area)
    private var taskGroupList = ArrayList<TaskGroup>()
    private val taskClickedFn = { task : Task -> taskClicked(task) }
    private val dateClickedFn = { group: Int -> groupClicked(group) }
    private lateinit var taskGroupAdapter: AdapterTaskGroup

    // Selecting tasks
    private var taskCount: Int = 0
    private var selected: Int = 0
    private var mode: Mode = Mode.ADD

    // Navigation TopBar
    private lateinit var topBar: ActionBar
    // Navigation bottom menu options
    private lateinit var taskAdd: MenuItem
    private lateinit var taskDelete: MenuItem
    private lateinit var taskSelectAll: MenuItem

    // Ensure you can only select either today or future dates, ToDo: Customizable
    private val calMaxDays = settings.calendarRange
    // Calendar limits + starting values
    private var startDate: String = ""
    private var startId: Int = 0
    private var minDate: Long = 0
    private var maxDate: Long = 0

    // Selectable dates
    private var dateList: ArrayList<String> = ArrayList()
    private lateinit var dates: Spinner

    // Saved/Loaded data using SharedPreferences
    private lateinit var saveLoad: SaveLoad

    // ########## Main functions ##########

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_topbar_menu, menu)

        // Create references to topBar menu options
        taskDelete = menu!!.findItem(R.id.delete)
        taskSelectAll = menu!!.findItem(R.id.selectAll)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.delete -> {
                taskGroupAdapter.deleteTasks(selected)
                taskCount -= selected

                // Clear selections and return to add mode
                setMode(Mode.ADD)
                updateSave()
                true
            }
            R.id.selectAll -> {
                // If not all selected, select all
                if (selected != taskGroupAdapter.taskCount) {
                    taskGroupAdapter.toggleAll()
                    selected = taskGroupAdapter.taskCount
                    updateSelectedCountDisplay()
                }
                // All selected, deselect all and return to add mode
                else {
                    taskGroupAdapter.toggleAll(false)
                    setMode(Mode.ADD)
                }
                true
            }
            else -> false
        }
        return super.onOptionsItemSelected(item)
    }

    // ########## Setup related functions ##########
    private fun runSetup() {
        // [1]. Initialize variable references
        setupLateInit()

        // [2]. Add custom toolbar at top
        setSupportActionBar(findViewById(R.id.mainBar))
        topBar = supportActionBar!!
        updateTopBar(mainTitle)

        // ToDo Remove: [2]. Set behaviour when clicking on bottom navigation toolbar
        bottomBarOld.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.menuAdd -> {
                    addNewTask()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupLateInit() {
        // ToDo Clickable date button to change date display
        dates = addTaskBar.findViewById(R.id.dates)

        // Bottom toolbar variables
        taskAdd = bottomBarOld.menu.findItem(R.id.menuAdd)

        // Add new task variables
        val cal = Calendar.getInstance()
        startDate = dateFormat.format(cal.timeInMillis)
        startId = idFormat.format(cal.timeInMillis).toInt()
        minDate = cal.timeInMillis
        // Add extra days to get max date
        cal.add(Calendar.DATE, calMaxDays)
        maxDate = cal.timeInMillis
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

    // ########## OnClick ##########
    fun dateClicked(view: View) {
        // Generate array of dates
        // Add dates to drop down list
    }

    private fun groupClicked(groupNum: Int) {
        val difference: Int = taskGroupAdapter.toggleGroup(groupNum)
        selected += difference

        // Changing modes depending on selection/deselection
        if (selected > 0)
            setMode(Mode.SELECTION)
        else
            setMode(Mode.ADD)
    }

    private fun taskClicked (task: Task) {
        // Update counts based on whether task selected/deselected
        if (task.selected) {
            selected++

            // Selected 0 -> 1, change to selection mode. Otherwise update as usual
            if (selected == 1)
                setMode(Mode.SELECTION)
            else
                updateSelectedCountDisplay()
        }
        else {
            selected--

            // Selected tasks 1 -> 0, return to add mode. Otherwise update as usual
            if (selected == 0)
                setMode(Mode.ADD)
            else
                updateSelectedCountDisplay()
        }
    }

    // ########## Change values/display ##########
    private fun setMode(newMode: Mode) {
        mode = newMode
        when (newMode) {
            Mode.ADD -> {
                // Set none selected and show main title
                selected = 0
                updateTopBar(mainTitle)

                // Disable modification options
                taskDelete.isVisible = false
                taskSelectAll.isVisible = false

                // Enable ability to add new tasks
                addTaskBar.visibility = View.VISIBLE
                bottomBarOld.visibility = View.VISIBLE
            }
            Mode.SELECTION -> {
                updateSelectedCountDisplay()

                // Show modification options
                taskDelete.isVisible = true
                taskSelectAll.isVisible = true

                // Disable ability to add new tasks
                addTaskBar.visibility = View.GONE
                bottomBarOld.visibility = View.GONE
            }
        }
    }

    private fun updateTopBar(newTitle: String) { topBar.title = newTitle }
    private fun updateSelectedCountDisplay() { updateTopBar("Selected: $selected") }

    // ########## Internal functions ##########
    private fun loadSave() {
        saveLoad = SaveLoad(this)
        taskGroupList = saveLoad.loadTaskGroupList()
        // settings = saveLoad.loadSettings()
        taskGroupAdapter = AdapterTaskGroup(taskGroupList, taskClickedFn, dateClickedFn, settings)
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
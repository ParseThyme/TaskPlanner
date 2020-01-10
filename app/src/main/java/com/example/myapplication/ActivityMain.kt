package com.example.myapplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.AdapterTaskGroup
import com.example.myapplication.data_classes.Task
import com.example.myapplication.data_classes.TaskGroup
import kotlinx.android.synthetic.main.main_activity.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    // Settings
    private val settings: Settings = Settings()

    // TaskList (Center Area)
    private var taskGroupList: ArrayList<TaskGroup> = ArrayList()
    private val taskClickedFn = { task : Task -> taskClicked(task) }
    private val dateClickedFn = { group: Int -> groupClicked(group) }
    private val toTopFn = { group: Int -> scrollTo(group) }
    private val updateSaveFn = { updateSave() }
    private lateinit var taskGroupAdapter: AdapterTaskGroup

    // Selecting tasks
    private var taskCount: Int = 0
    private var selected: Int = 0
    private var mode: Mode = Mode.ADD

    // Ensure you can only select either today or future dates, ToDo: Customizable
    private val calMaxDays = settings.calendarRange
    // Calendar limits + starting values
    private var date: String = ""
    private var startDate: String = ""
    private var id: Int = 0

    // Saved/Loaded data using SharedPreferences
    private lateinit var saveLoad: SaveLoad

    // ########## Main ##########
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
        dateGroupRV.addDivider()

        runSetup()
        setMode(Mode.ADD)
    }

    // ########## Setup related ##########
    private fun runSetup() {
        // [1]. Initialize variable references
        setupLateInit()

        // [2]. Buttons (topBar and bottomBar)
        setupButtons()
    }

    private fun setupLateInit() {
        // Add new task variables
        val cal = Calendar.getInstance()
        // Apply starting date to be today's date at bottom bar
        btnChangeDate.text = createDateLabel(cal, true)

        // Create starting date, id and min date
        startDate = createDateLabel(cal)
        id = idFormat.format(cal.timeInMillis).toInt()
        minDate = cal.timeInMillis
        // Add extra days to get max date
        cal.add(Calendar.DATE, calMaxDays)
        maxDate = cal.timeInMillis
        // Reset date back to starting date
        date = startDate

        // Input Validation:
        // TextWatcher. Ensure confirm button only enabled when task entered (can't submit blank tasks)
        if (validateInput) {
            btnNewTask.isEnabled = false
            btnNewTask.toggle(false)

            taskDesc.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(s: Editable) { }
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                // Check when text is being changed
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    // Toggle confirm button based on whether text is empty or not
                    btnNewTask.isEnabled = taskDesc.text.isNotEmpty()
                    btnNewTask.toggle(btnNewTask.isEnabled)
                }
            })
        }

        // Settings
        taskDesc.setMaxLength(settings.taskMaxLength)
    }

    private fun setupButtons() {
        // ########## TopBar ##########
        // 0. ToDo: Menu button

        // 1. Select all
        btnSelectAll.setOnClickListener {
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
        }

        // 2. Delete selected
        btnDelete.setOnClickListener {
            taskGroupAdapter.deleteSelected(selected)
            taskCount -= selected

            // Clear selections and return to add mode
            setMode(Mode.ADD)
            updateSave()
        }

        // 3. Settings
        btnSettings.setOnClickListener {
            // ToDo
        }

        // ########## BottomBar ##########
        // 1. Update date
        // Setup listener for date picker dialog
        val cal = Calendar.getInstance()
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, y, m, d ->
                cal.set(Calendar.YEAR, y)
                cal.set(Calendar.MONTH, m)
                cal.set(Calendar.DAY_OF_MONTH, d)

                date = createDateLabel(cal)
                id = idFormat.format(cal.timeInMillis).toInt()
                btnChangeDate.text = createDateLabel(cal, true)
            }
        btnChangeDate.setOnClickListener {
            val dialog = DatePickerDialog(this, dateSetListener,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            )

            // Assign min + max date then show dialog box
            dialog.datePicker.minDate = minDate
            dialog.datePicker.maxDate = maxDate
            dialog.show()
        }

        // 2. Add new task
        btnNewTask.setOnClickListener {
            // Get task description entry, create task entry and add to adapter
            val desc = taskDesc.text.toString().trim()
            taskGroupAdapter.addTask(id, date, desc)

            // Clear task entry and clear focus
            taskDesc.setText("")
            taskDesc.clearFocus()
            taskDesc.hideKeyboard()

            updateSave()
        }
    }

    // ########## OnClick ##########
    private fun groupClicked(groupNum: Int) {
        val difference: Int = taskGroupAdapter.toggleGroupSelected(groupNum)
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

    // Scroll to position when group opened/closed (accounts for opening/closing top/bottom)
    private fun scrollTo(position: Int) {
        dateGroupRV.scrollToPosition(position)

        // Scroll bit extra for last position
        if (position == taskGroupList.lastIndex) {
            (dateGroupRV.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 20)
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

                // Disable modification buttons and disable options
                btnDelete.visibility = View.GONE
                btnSelectAll.visibility = View.GONE
                //btnSettings.visibility = View.VISIBLE

                // Enable ability to add new tasks
                bottomBar.visibility = View.VISIBLE
            }
            Mode.SELECTION -> {
                updateSelectedCountDisplay()

                // Show modification buttons and disable options
                btnDelete.visibility = View.VISIBLE
                btnSelectAll.visibility = View.VISIBLE
                //btnSettings.visibility = View.GONE

                // Disable ability to add new tasks
                bottomBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun updateTopBar(newTitle: String) { topBarTitle.text = newTitle }
    private fun updateSelectedCountDisplay() { updateTopBar("Selected: $selected") }

    private fun ImageButton.toggle(enabled: Boolean) {
        if (enabled)
            updateBtnColor(R.color.btnEnabled, applicationContext)
        else
            updateBtnColor(R.color.btnDisabled, applicationContext)
    }

    // ########## Internal functions ##########
    private fun loadSave() {
        saveLoad = SaveLoad(this)
        taskGroupList = saveLoad.loadTaskGroupList()
        // settings = saveLoad.loadSettings()
        taskGroupAdapter = AdapterTaskGroup(taskGroupList, settings,
            taskClickedFn, dateClickedFn, toTopFn, updateSaveFn)
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
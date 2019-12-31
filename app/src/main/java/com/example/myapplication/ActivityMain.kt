package com.example.myapplication

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
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
    private lateinit var taskDelete: MenuItem
    private lateinit var taskSelectAll: MenuItem

    // Ensure you can only select either today or future dates, ToDo: Customizable
    private val calMaxDays = settings.calendarRange
    // Calendar limits + starting values
    private var date: String = ""
    private var startDate: String = ""
    private var id: Int = 0

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
        taskSelectAll = menu.findItem(R.id.selectAll)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.delete -> {
                taskGroupAdapter.deleteSelected(selected)
                taskCount -= selected

                // Clear selections and return to add mode
                setMode(Mode.ADD)
                updateSave()
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
            }
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

        // Setup listener for date picker dialog
        val cal = Calendar.getInstance()
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, y, m, d ->
                cal.set(Calendar.YEAR, y)
                cal.set(Calendar.MONTH, m)
                cal.set(Calendar.DAY_OF_MONTH, d)

                date = dateFormat.format(cal.timeInMillis)
                id = idFormat.format(cal.timeInMillis).toInt()
                changeDateBtn.text = date
            }

        // ########## Buttons ##########
        // A. Update date button
        changeDateBtn.setOnClickListener {
            val dialog = DatePickerDialog(this, dateSetListener,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            )

            // Assign min + max date then show dialog box
            dialog.datePicker.minDate = minDate
            dialog.datePicker.maxDate = maxDate
            dialog.show()
        }

        // B. Add new task button
        newTaskBtn.setOnClickListener {
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

    private fun setupLateInit() {
        // Add new task variables
        val cal = Calendar.getInstance()
        startDate = dateFormat.format(cal.timeInMillis)
        id = idFormat.format(cal.timeInMillis).toInt()
        minDate = cal.timeInMillis
        // Add extra days to get max date
        cal.add(Calendar.DATE, calMaxDays)
        maxDate = cal.timeInMillis
        // Reset date back to starting date
        date = startDate

        // Apply starting date to be today's date at bottom bar
        changeDateBtn.text = date

        // Input Validation:
        // TextWatcher. Ensure confirm button only enabled when task entered (can't submit blank tasks)
        if (validateInput) {
            newTaskBtn.isEnabled = false
            newTaskBtn.setColorFilter(Color.GRAY)

            taskDesc.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(s: Editable) { }
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                // Check when text is being changed
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    // Toggle confirm button based on whether text is empty or not
                    newTaskBtn.isEnabled = taskDesc.text.isNotEmpty()

                    if (newTaskBtn.isEnabled)
                        newTaskBtn.setColorFilter(Color.GREEN)
                    else
                        newTaskBtn.setColorFilter(Color.GRAY)
                }
            })
        }
    }

    // ########## OnClick ##########
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
                bottomBar.visibility = View.VISIBLE
            }
            Mode.SELECTION -> {
                updateSelectedCountDisplay()

                // Show modification options
                taskDelete.isVisible = true
                taskSelectAll.isVisible = true

                // Disable ability to add new tasks
                bottomBar.visibility = View.GONE
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
        taskGroupAdapter = AdapterTaskGroup(taskGroupList, taskClickedFn, dateClickedFn, { updateSave() }, settings)
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
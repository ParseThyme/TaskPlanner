package com.example.myapplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ImageButton
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.TaskGroupAdapter
import com.example.myapplication.data_classes.Tag
import com.example.myapplication.data_classes.Task
import com.example.myapplication.data_classes.TaskGroup
import com.example.myapplication.data_classes.setImageResourceFromTag
import kotlinx.android.synthetic.main.main_view.*
import kotlinx.android.synthetic.main.tag_popup_window.view.*
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
    private lateinit var taskGroupAdapter: TaskGroupAdapter

    // Selecting tasks
    private var taskCount: Int = 0
    private var selected: Int = 0
    private var mode: Mode = Mode.START

    // Ensure you can only select either today or future dates, ToDo: Customizable
    private val calMaxDays = settings.calendarRange
    // Calendar limits + starting values
    private var startDate: String = ""
    private var id: Int = 0

    // Created task
    private var date: String = ""
    private var tag: Tag = Tag.NONE

    // Saved/Loaded data using SharedPreferences
    private lateinit var saveLoad: SaveLoad

    // ########## Main ##########
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_view)

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

        // [3]. Popup menus (bottomBar)
        setupPopupMenus()
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

        // Settings
        // taskDesc.setMaxLength(settings.taskMaxLength)
    }

    private fun setupButtons() {
        // ########## TopBar ##########
        // 0. ToDo: Menu button

        // 1. Select all
        btnSelectAll.setOnClickListener {
            // If not all selected, select all
            if (selected != taskGroupAdapter.taskCount) {
                // Change icon to opposite icon (deselect all)
                btnSelectAll.setImageResource(R.drawable.ic_select_all_off)

                // Toggle all to selected state
                taskGroupAdapter.toggleAll()
                selected = taskGroupAdapter.taskCount
                updateSelectedCountDisplay()

                // Switch to select mode if in add mode
                setMode(Mode.SELECTION)
            }
            // All selected, deselect all and return to add mode
            else {
                // Toggle all to off state and return to add mode
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
            taskGroupAdapter.addTask(id, date, desc, tag)

            // Reset values
            taskDesc.setText("")
            taskDesc.clearFocus()
            taskDesc.hideKeyboard()

            // Save changes
            updateSave()
        }

        // 3. Set time
        btnTime.text = "Set Time"
        btnTime.setOnClickListener {
            // ToDo
        }
    }

    private fun setupPopupMenus() {
        // Setting task tag
        btnTag.setOnClickListener {
            val window:PopupWindow = createTagPopupWindow(btnTag)
            window.contentView.tagGroup.setOnCheckedChangeListener { _, chosenTag ->
                when (chosenTag) {
                    R.id.tagNone -> tag = Tag.NONE
                    R.id.tagEvent -> tag = Tag.EVENT
                    R.id.tagBooking -> tag = Tag.BOOKING
                    R.id.tagBuy -> tag = Tag.BUY
                }

                btnTag.setImageResourceFromTag(tag)
                window.dismiss()
            }
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
            else {
                updateSelectedCountDisplay()

                // If all selected, change topBar icon (selectAll to off)
                if (selected == taskGroupAdapter.taskCount)
                    btnSelectAll.setImageResource(R.drawable.ic_select_all_off)

            }
        }
        else {
            selected--

            // Selected tasks 1 -> 0, return to add mode. Otherwise update as usual
            if (selected == 0)
                setMode(Mode.ADD)
            else {
                updateSelectedCountDisplay()

                // If went from max to max - 1, change topBar icon (selectAll to on)
                if (selected == taskGroupAdapter.taskCount - 1)
                    btnSelectAll.setImageResource(R.drawable.ic_select_all_on)
            }
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
        // Do nothing if called on same mode
        if (mode == newMode)
            return

        mode = newMode
        when (newMode) {
            Mode.ADD -> {
                // Set none selected and show main title
                selected = 0
                updateTopBar(mainTitle)

                // Switch display of bottomBar
                addModeBar.visibility = View.VISIBLE
                selectModeBar.visibility = View.GONE

                // Reset icon: Select all
                btnSelectAll.setImageResource(R.drawable.ic_select_all_on)

            }
            Mode.SELECTION -> {
                updateSelectedCountDisplay()

                // Switch display of bottomBar
                addModeBar.visibility = View.GONE
                selectModeBar.visibility = View.VISIBLE
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
        taskGroupAdapter = TaskGroupAdapter(taskGroupList, settings,
            taskClickedFn, dateClickedFn, toTopFn, updateSaveFn)
    }

    private fun updateSave() { saveLoad.saveTaskGroupList(taskGroupList) }

    /*
    private fun deleteSave() {
        saveLoad.clearAllData()
    }
    */
}

enum class Mode { START, ADD, SELECTION }
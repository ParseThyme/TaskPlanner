package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.TaskGroupAdapter
import com.example.myapplication.data_classes.*
import com.example.myapplication.popup_windows.*
// import com.example.myapplication.popup_windows.createDatePopup
import kotlinx.android.synthetic.main.main_activity_view.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    // Settings
    // private val settings: Settings = Settings()

    // TaskList (Center Area)
    private var taskGroupList: ArrayList<TaskGroup> = ArrayList()
    private val taskClickedFn = { task : Task -> taskClicked(task) }
    private val dateClickedFn = { group: Int -> groupClicked(group) }
    private val toTopFn = { group: Int -> scrollTo(group) }
    private val updateCollapseExpandIconFn = { state: ViewState -> updateCollapseExpandIcon(state)}
    private val updateSaveFn = { updateSave() }
    private lateinit var taskGroupAdapter: TaskGroupAdapter

    // Selecting tasks
    private var taskCount: Int = 0
    private var selected: Int = 0
    private var mode: Mode = Mode.START

    // Created task
    private var newTask: Task = Task()
    private var newDate: TaskDate = today()
    private val today: TaskDate = today()
    private lateinit var keyboard: Keyboard

    // Saved/Loaded data using SharedPreferences
    private lateinit var saveLoad: SaveLoad

    // ########## Main ##########
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_view)

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
        setDefaultValues()

        // Initialize variable references
        // Apply starting date to be today's date at bottom bar
        btnSetDate.text = today.createLabel(Size.Med)
        keyboard = Keyboard(txtTaskDesc)
        keyboard.addInputValidation(btnNewTask)

        // Buttons (topBar and bottomBar)
        setupButtons()
    }

    private fun setDefaultValues() {
        btnSetTime.text = defaultTimeMsg
    }

    // ########## Buttons ##########
    private fun setupButtons() {
        // ##############################
        // TopBar
        // ##############################
        btnSelectAll.setOnClickListener { selectAllBtnFn() }
        btnCollapseExpand.setOnClickListener { collapseExpandBtnFn() }
        btnSettings.setOnClickListener { settingsBtnFn() }

        // ##############################
        // BottomBar
        // ##############################

        // Add mode
        btnNewTask.setOnClickListener { newTaskBtnFn() }
        btnReset.setOnClickListener { resetBtnFn() }
        btnSetDate.setOnClickListener { PopupManager.datePopup(bottomBar, btnSetDate, this, newDate) }
        btnSetTime.setOnClickListener { PopupManager.timePopup(bottomBar, btnSetTime, this, newTask) }
        btnSetTag.setOnClickListener  { PopupManager.tagPopup(bottomBar, btnSetTag, this, newTask) }

        // Select mode
        btnDelete.setOnClickListener { deleteBtnFn() }
    }

    private fun newTaskBtnFn() {
        // Get relevant values
        newTask.desc = txtTaskDesc.text.toString().trim()
        val time: TaskTime = newTask.time
        val date: TaskDate = newDate
        val tag: TaskTag = newTask.tag

        // Add new task to adapter
        taskGroupAdapter.addTask(date, newTask.copy())

        // Reset values
        txtTaskDesc.setText("")
        txtTaskDesc.clearFocus()

        // Save changes
        updateSave()
    }

    private fun selectAllBtnFn() {
        // If not all selected, select all
        if (selected != taskGroupAdapter.taskCount) {
            // Change icon to opposite icon (deselect all)
            btnSelectAll.setImageResource(R.drawable.ic_select_all_off)

            // Toggle all to selected state
            taskGroupAdapter.toggleAllHighlight()
            selected = taskGroupAdapter.taskCount
            updateSelectedCountDisplay()

            // Switch to select mode if in add mode
            setMode(Mode.SELECTION)
        }
        // All selected, deselect all and return to add mode
        else {
            // Toggle all to off state and return to add mode
            taskGroupAdapter.toggleAllHighlight(false)
            setMode(Mode.ADD)
        }
    }

    private fun collapseExpandBtnFn() {
        // Expand all when all are collapsed, switch icon to collapse all icon
        if (taskGroupAdapter.allCollapsed()) {
            taskGroupAdapter.toggleAllExpandCollapse()
            updateCollapseExpandIcon(ViewState.EXPANDED)
        }
        // Otherwise collapse all and switch icon to expand all icon
        else {
            taskGroupAdapter.toggleAllExpandCollapse(ViewState.COLLAPSED)
            updateCollapseExpandIcon(ViewState.COLLAPSED)
        }
        updateSave()
    }

    //ToDo
    private fun settingsBtnFn() { }

    private fun deleteBtnFn() {
        taskGroupAdapter.deleteSelected(selected)
        taskCount -= selected

        // Clear selections and return to add mode
        setMode(Mode.ADD)
        updateSave()
    }

    //ToDo
    private fun resetBtnFn() {}

    // ########## Popups ##########

    // ########## OnClick ##########
    private fun groupClicked(groupNum: Int) {
        val difference: Int = taskGroupAdapter.toggleGroupHighlight(groupNum)
        val selectedPreClick = selected
        selected += difference

        when {
            // [1]. From 0 -> x selected. Enter select mode
            selectedPreClick == 0 -> {
                setMode(Mode.SELECTION)
            }
            // [2]. From x -> 0 selected. Return to add mode
            selected == 0 -> {
                setMode(Mode.ADD)
            }
            // [3]. From x -> x + y OR x -> x - y. Update value display
            else -> { updateSelectedCountDisplay() }
        }
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
            else -> return
        }
    }

    private fun updateTopBar(newTitle: String) { topBarTitle.text = newTitle }
    private fun updateSelectedCountDisplay() { updateTopBar("Selected: $selected") }

    private fun updateCollapseExpandIcon(state: ViewState) {
        when(state) {
            ViewState.EXPANDED -> btnCollapseExpand.setImageResource(R.drawable.ic_view_collapse)
            ViewState.COLLAPSED -> btnCollapseExpand.setImageResource(R.drawable.ic_view_expand)
        }
    }

    // ########## Save/Load ##########
    private fun loadSave() {
        saveLoad = SaveLoad(this)
        // saveLoad.clearAllData()
        taskGroupList = saveLoad.loadTaskGroupList()
        // settings = saveLoad.loadSettings()
        taskGroupAdapter =
            TaskGroupAdapter(taskGroupList, taskClickedFn, dateClickedFn, toTopFn,
                             updateCollapseExpandIconFn, updateSaveFn)
    }

    private fun updateSave() { saveLoad.saveTaskGroupList(taskGroupList) }

    /*
    private fun deleteSave() {
        saveLoad.clearAllData()
    }
    */

    // ########## Utility ##########
    // Scroll to position when group opened/closed (accounts for opening/closing top/bottom)
    private fun scrollTo(position: Int) {
        dateGroupRV.scrollToPosition(position)

        // Scroll bit extra for last position
        if (position == taskGroupList.lastIndex) {
            (dateGroupRV.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                position,
                20
            )
        }
    }
}

enum class Mode { START, ADD, SELECTION }
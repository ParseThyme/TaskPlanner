package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.TaskGroupAdapter
import com.example.myapplication.data_classes.*
import com.example.myapplication.popups.PopupSavedTasks
import com.example.myapplication.singletons.*
import kotlinx.android.synthetic.main.main_activity_view.*
import kotlinx.android.synthetic.main.main_topbar.view.*
import kotlinx.android.synthetic.main.main_mode_add.view.*
import kotlinx.android.synthetic.main.main_mode_select.view.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    // Settings
    // private val settings: Settings = Settings()

    // TaskList (Center Area)
    private val clickTaskFn = { state: Boolean, group: Int, task: Int -> taskClicked(state, group, task) }
    private val clickDateFn = { group: Int -> groupClicked(group) }
    private val scrollToFn = { group: Int -> scrollToGroup(group) }
    private val updateFoldIconFn = { state: Fold -> toggleFoldIcon(state)}

    // Toggled
    private var mode: Mode = Mode.START

    // Selection
    private var recentlyClickedGroup: Int = 0
    private var recentlyClickedTask: Int = 0

    // Created task
    private var newTask: Task = Task()
    private var newDate: TaskDate = today()
    // Modified tasks
    private var selectModeDate: TaskDate = today()

    // Data
    private var taskGroupList: ArrayList<GroupEntry> = ArrayList()
    private var saveTask: Boolean = false

    // Late initialized variables
    private lateinit var taskGroupAdapter: TaskGroupAdapter

    // ########## Main ##########
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_view)

        // 1. Load data, create adapter from loaded groupList (or create new one)
        loadSave()
        taskGroupAdapter =
            TaskGroupAdapter(taskGroupList, clickTaskFn, clickDateFn, scrollToFn, updateFoldIconFn)

        // 2. Assign layout manager and adapter to recycler view and set initial layout to show
        Settings.initMainLayout(dateGroupRV, taskGroupAdapter)
        titleBar.toggleLayout.isChecked = Settings.layoutAsBoolean()

        // 3. Setup singletons
        Keyboard.setup(this, addMode.txtTaskDesc)
        Keyboard.addInputValidation(addMode.btnAddNewTask)

        // 4. Initialize button click listeners
        setupButtons()                                      // Buttons (topBar and bottomBar)

        // 5. Setup labels
        newTask.time.unset()
        titleBar.title.text = mainTitle                     // Main App name
        addMode.txtSetDate.text = newDate.asStringShort()   // Starting date to be today at bottom bar
        addMode.txtSetTime.text = defaultTimeMsg            // Set time to be blank

        // ToDo: Reorganize
        /*
        val tasks: ArrayList<String> = arrayListOf(
            "Task 1", "Task 2", "Task 3", "Task 4", "Task 5",
            "Task 6", "Task 7", "Task 8", "Task 9", "Task 10")
        */
        val tasks: ArrayList<String> = arrayListOf()
        when (tasks.size) {
               0 -> addMode.toggleSavedTasksPopup.visibility = View.GONE
            else -> addMode.toggleSavedTasksPopup.visibility = View.VISIBLE
        }
        val popupTasks = PopupSavedTasks(tasks)
        addMode.toggleSavedTasksPopup.setOnClickListener {
            // Open popup window, update icon as checked
            addMode.toggleSavedTasksPopup.isChecked = true
            val window: PopupWindow = popupTasks.create(addMode.txtTaskDesc, addMode.txtTaskDesc,
                addMode.toggleSavedTasksPopup.context, newTask)
            // Switch icon back to unchecked when window closed
            window.setOnDismissListener { addMode.toggleSavedTasksPopup.isChecked = false }
        }
        // ToDo: END

        setMode(Mode.ADD)
    }

    // ########## Setup, Buttons, OnClick ##########
    private fun setupButtons() {
        // ##############################
        // TopBar
        // ##############################
        titleBar.toggleFold.setOnClickListener { taskGroupAdapter.toggleFoldAll(titleBar.toggleFold.context) }
        titleBar.toggleLayout.setOnClickListener { Settings.toggleLayout(titleBar.toggleLayout.context) }
        // btnSettings.setOnClickListener { }

        // ##############################
        // BottomBar
        // ##############################
        // 1. Add Mode
        addMode.btnAddNewTask.setOnClickListener {
            // Get relevant values
            val desc: String = addMode.txtTaskDesc.text.toString().trim()
            val time: TaskTime = newTask.time.copy()
            val date: TaskDate = newDate.copy()
            val tag: Int = newTask.tag
            val addedTask = Task(desc, tag, time)

            // Add new task to adapter
            taskGroupAdapter.addTask(date, addedTask)

            // Reset text entry and time
            addMode.txtTaskDesc.setText("")
            newTask.time.unset()
            addMode.txtSetTime.text = defaultTimeMsg

            // Save changes
            SaveData.saveTaskGroupList(taskGroupList, addMode.btnAddNewTask.context)
        }

        addMode.txtSetDate.setOnClickListener {
            PopupManager.date.create(newDate, this, addMode, addMode.txtSetDate) }
        addMode.txtSetTime.setOnClickListener {
            PopupManager.time.create(newTask.time, this, addMode, addMode.txtSetTime) }
        addMode.btnSetTag.setOnClickListener  {
            PopupManager.tag.create(newTask, this, addMode, addMode.btnSetTag) }

        addMode.btnResetParams.setOnClickListener {
            // Reset all values (exclude text entry)
            newTask.tag = R.drawable.tag_base
            newDate = today()
            newTask.time.unset()
            addMode.txtSetTime.text = defaultTimeMsg

            // Update views
            addMode.btnSetTag.updateDrawableTop(newTask.tag)
            addMode.txtSetTime.text = defaultTimeMsg
            addMode.txtSetDate.text = newDate.asStringShort()
        }

        // 2. Select Mode
        selectMode.btnSelectAll.setOnClickListener {
            when (AppData.allSelected()) {
                // Not all selected, select all
                false -> {
                    taskGroupAdapter.toggleSelectAll()
                    updateSelectedCountDisplay()
                }

                // Deselect all except for initially selected task
                true -> {
                    taskGroupAdapter.toggleSelectAll(false)
                    taskGroupAdapter.select(recentlyClickedGroup, recentlyClickedTask)
                    updateSelectedCountDisplay()
                }
            }
        }
        selectMode.btnSelectNone.setOnClickListener {
            // Toggle all to off state and return to add mode
            taskGroupAdapter.toggleSelectAll(false)
            setMode(Mode.ADD)
        }

        selectMode.btnToDate.setOnClickListener {
            // 1. Create window, user selects new date
            // 2. Override date for selected tasks in adapter
            val window: PopupWindow = PopupManager.date.create(selectModeDate, this, selectMode)
            window.setOnDismissListener {
                // Apply changes to selected date when apply button pressed
                if (PopupManager.date.update) {
                    taskGroupAdapter.selectedSetDate(selectModeDate)
                    setMode(Mode.ADD)
                    SaveData.saveTaskGroupList(taskGroupList, selectMode.btnToDate.context)
                }
            }
        }
        selectMode.btnToTime.setOnClickListener {
            val newTime = TaskTime()
            val window: PopupWindow = PopupManager.time.create(newTime, this, selectMode)
            window.setOnDismissListener {
                if (PopupManager.time.update) {
                    taskGroupAdapter.selectedSetTime(newTime)
                    SaveData.saveTaskGroupList(taskGroupList, selectMode.btnToTime.context)
                }
            }
        }
        selectMode.btnToTag.setOnClickListener {
            val newTag = Task()
            val window:PopupWindow = PopupManager.tag.create(newTag, this, selectMode)
            window.setOnDismissListener {
                if (PopupManager.tag.update) {
                    taskGroupAdapter.selectedSetTag(newTag.tag)
                    SaveData.saveTaskGroupList(taskGroupList, selectMode.btnToTag.context)
                }
            }
        }
        selectMode.btnClearParams.setOnClickListener {
            taskGroupAdapter.selectedClearAll()
            setMode(Mode.ADD)
            SaveData.saveTaskGroupList(taskGroupList, applicationContext)
        }
        selectMode.btnDelete.setOnClickListener {
            taskGroupAdapter.delete()
            setMode(Mode.ADD)
            SaveData.saveTaskGroupList(taskGroupList, applicationContext)
        }
    }

    private fun groupClicked(groupNum: Int) {
        val difference: Int = taskGroupAdapter.toggleGroupSelected(groupNum)
        val selectedPreClick: Int = AppData.numSelected
        AppData.numSelected += difference

        when {
            selectedPreClick == 0 -> setMode(Mode.SELECTION)    // [1]. From 0 -> x selected. Enter select mode
            AppData.numSelected == 0 -> {                   // [2]. From x -> 0 selected. Return to add mode
                setMode(Mode.ADD)
                return
            }
        }

        // [3]. Between 1-numTasks: Update display & track most recently selected group/task
        updateSelectedCountDisplay()
        recentlyClickedGroup = groupNum
        recentlyClickedTask = 0
    }
    private fun taskClicked(selected: Boolean, groupIndex: Int, taskIndex: Int) {
        // Update counts based on whether task selected/deselected
        when (selected) {
            // Check if 0 -> 1. Going from Add -> SelectMode
            true -> {
                if (AppData.numSelected == 1) setMode(Mode.SELECTION)
                // Track most recently selected group/task
                recentlyClickedGroup = groupIndex
                recentlyClickedTask = taskIndex
            }
            // 1 -> 0. Return to addMode
            false -> {
                if (AppData.numSelected == 0) {
                    setMode(Mode.ADD)
                    return
                }
            }
        }
        // Otherwise update count display for any other condition
        updateSelectedCountDisplay()
    }

    override fun onBackPressed() {
        // Override what system back key does
        if (mode == Mode.SELECTION) { // Return to AddMode
            taskGroupAdapter.toggleSelectAll(false)
            setMode(Mode.ADD)
        }
        else moveTaskToBack(true)
    }

    // ########## Change values/display ##########
    private fun setMode(newMode: Mode) {
        // Do nothing if called on same mode
        if (mode == newMode) return

        mode = newMode
        when (newMode) {
            Mode.ADD -> {
                // Switch display of bottomBar
                addMode.visibility = View.VISIBLE
                selectMode.visibility = View.GONE
            }
            else -> {   // MODE.SELECTION
                // Show numSelected
                updateSelectedCountDisplay()
                // Switch display of bottomBar
                addMode.visibility = View.GONE
                selectMode.visibility = View.VISIBLE
            }
        }
    }
    private fun updateSelectedCountDisplay() { selectMode.txtSelected.text = AppData.numSelectedMsg() }

    private fun toggleFoldIcon(state: Fold) { titleBar.toggleFold.isChecked = state.asBoolean() }

    // ########## Save/Load ##########
    private fun loadSave() {
        // Uncomment for broken data
        // saveLoad.clearAllData()

        // Load data
        taskGroupList = SaveData.loadTaskGroupList(applicationContext)

        // Load settings
        Settings.init(
            SaveData.loadLayout(applicationContext),
                      SaveData.loadTimeDelta(applicationContext))
    }

    // ########## Utility ##########
    // Scroll to position when group opened/closed (accounts for opening/closing top/bottom)
    private fun scrollToGroup(position: Int) {
        if (Settings.usingGridLayout()) return

        dateGroupRV.scrollToPosition(position)
        // Scroll bit extra for last position
        if (position == taskGroupList.lastIndex)
            (dateGroupRV.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 20)
    }
}
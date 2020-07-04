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
    private var storedTasks: ArrayList<Task> = ArrayList()

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
        when (storedTasks.size) {
               0 -> addMode.toggleSavedTasksPopup.visibility = View.GONE
            else -> addMode.toggleSavedTasksPopup.visibility = View.VISIBLE
        }

        // 1. Add Mode
        addMode.btnAddNewTask.setOnClickListener {
            // Get relevant values
            val addedTask = Task(addMode.txtTaskDesc.text.toString().trim(),    // Description
                            newTask.tag,                                        // Tag
                            newTask.time.copy())                                // Time

            // Add new task to adapter
            taskGroupAdapter.addTask(newDate.copy(), addedTask)

            // Reset text entry and time
            newTask.time.unset()
            addMode.txtTaskDesc.setText("")
            addMode.txtSetTime.text = defaultTimeMsg

            // Save change to list
            SaveData.saveTaskGroupList(taskGroupList, addMode.btnAddNewTask.context)

            // Save task if Star toggled on
            if (addMode.toggleStoreTask.isChecked) {
                storedTasks.add(addedTask.copy())
                SaveData.saveStoredTaskList(storedTasks, addMode.btnAddNewTask.context)

                // First task saved. Show toggle arrow icon
                if (storedTasks.size > 0) {
                    addMode.toggleSavedTasksPopup.visibility = View.VISIBLE
                }

                addMode.toggleStoreTask.isChecked = false
            }
        }
        addMode.toggleSavedTasksPopup.setOnClickListener {
            // Open popup window, update icon as checked
            addMode.toggleSavedTasksPopup.isChecked = true

            val popupTasks = PopupSavedTasks()
            val window: PopupWindow =
                popupTasks.create(addMode.addTaskDescLayout, addMode.txtTaskDesc, newTask, storedTasks)

            window.setOnDismissListener {
                // When window closed reverse arrow icon back
                addMode.toggleSavedTasksPopup.isChecked = false
                // Check if storedTasks has been modified, if so save changes made
                if (popupTasks.modified) {
                    SaveData.saveStoredTaskList(storedTasks, addMode.toggleSavedTasksPopup.context)

                    // If list is completely empty hide arrow
                    if (storedTasks.size == 0)
                        addMode.toggleSavedTasksPopup.visibility = View.GONE
                }
            }
        }

        addMode.txtSetDate.setOnClickListener { Popups.date.create(newDate, addMode, addMode.txtSetDate) }
        addMode.txtSetTime.setOnClickListener { Popups.time.create(newTask.time, addMode, addMode.txtSetTime) }
        addMode.btnSetTag.setOnClickListener  { Popups.tag.create(newTask, addMode, addMode.btnSetTag) }

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
            val window: PopupWindow =
                Popups.date.create(selectModeDate, selectMode)
            window.setOnDismissListener {
                // Apply changes to selected date when apply button pressed
                if (Popups.date.update) {
                    taskGroupAdapter.selectedSetDate(selectModeDate)
                    setMode(Mode.ADD)
                    SaveData.saveTaskGroupList(taskGroupList, selectMode.btnToDate.context)
                }
            }
        }
        selectMode.btnToTime.setOnClickListener {
            val newTime = TaskTime()
            val window: PopupWindow =
                Popups.time.create(newTime, selectMode)
            window.setOnDismissListener {
                if (Popups.time.update) {
                    taskGroupAdapter.selectedSetTime(newTime)
                    SaveData.saveTaskGroupList(taskGroupList, selectMode.btnToTime.context)
                }
            }
        }
        selectMode.btnToTag.setOnClickListener {
            val newTag = Task()
            val window:PopupWindow =
                Popups.tag.create(newTag, selectMode)
            window.setOnDismissListener {
                if (Popups.tag.update) {
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
        storedTasks = SaveData.loadStoredTaskList(applicationContext)

        // Load settings
        Settings.init(SaveData.loadLayout(applicationContext),
                      SaveData.loadTimeDelta(applicationContext))

        // Store navigation bar size
        AppData.navBarSize = getNavigationBarSize(applicationContext)!!
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
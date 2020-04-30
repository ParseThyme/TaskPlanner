package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.TaskGroupAdapter
import com.example.myapplication.data_classes.*
import com.example.myapplication.popups.PopupManager
import com.example.myapplication.utility.*
import kotlinx.android.synthetic.main.main_activity_view.*
import kotlinx.android.synthetic.main.main_layout_top_view.view.*
import kotlinx.android.synthetic.main.main_mode_add_view.view.*
import kotlinx.android.synthetic.main.main_mode_select_view.view.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    // Settings
    // private val settings: Settings = Settings()

    // TaskList (Center Area)
    private val clickTaskFn = { task : Task -> taskClicked(task) }
    private val clickDateFn = { group: Int -> groupClicked(group) }
    private val toTopFn = { group: Int -> scrollTo(group) }
    private val updateFoldIconFn = { state: Fold -> toggleFoldIcon(state)}
    private val updateSaveFn = { updateSave() }

    // Toggled
    private var mode: Mode = Mode.START

    // Created task
    private var newTask: Task = Task()
    private var newDate: TaskDate = today()

    // Data
    private var taskGroupList: ArrayList<TaskGroup> = ArrayList()
    private var tagsList: ArrayList<Int> = ArrayList()

    // Late initialized variables
    private lateinit var saveLoad: SaveLoad
    private lateinit var taskGroupAdapter: TaskGroupAdapter

    // ########## Main ##########
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_view)

        Settings.init()

        // Create adapter from loaded groupList (or create new one)
        loadSave()
        taskGroupAdapter = TaskGroupAdapter(taskGroupList, clickTaskFn, clickDateFn, toTopFn,
                                            updateFoldIconFn, updateSaveFn)

        // Assign layout manager and adapter to recycler view and set initial button resource to show
        Settings.initMainLayout(dateGroupRV, taskGroupAdapter)
        toggleLayoutButton()

        runSetup()
        setMode(Mode.ADD)
    }

    // ########## Setup related ##########
    private fun runSetup() {
        // Setup singletons
        Keyboard.setup(this, addMode.txtTaskDesc)
        Keyboard.addInputValidation(addMode.btnNewTask)

        // ToDo: Implement tags list to be saved and loaded
        tagsList = arrayListOf (
            R.drawable.tag_booking,
            R.drawable.tag_mail,
            R.drawable.tag_delivery,
            R.drawable.tag_flight,
            R.drawable.tag_read,
            R.drawable.tag_medicine,

            R.drawable.tag_event,
            R.drawable.tag_buy,
            R.drawable.tag_food,
            R.drawable.tag_movie,
            R.drawable.tag_pet,
            R.drawable.tag_workout,

            R.drawable.tag_important,
            R.drawable.tag_one,
            R.drawable.tag_two,
            R.drawable.tag_three,
            R.drawable.tag_four,
            R.drawable.tag_five
        )
        PopupManager.setup(tagsList)

        // Initialize variable references
        // Apply starting date to be today's date at bottom bar
        addMode.txtSetDate.text = Settings.today.asStringShort()
        // Set time to be blank
        newTask.time.clear(addMode.txtSetTime)

        // Buttons (topBar and bottomBar)
        setupButtons()
    }

    // ########## Buttons ##########
    private fun setupButtons() {
        // ##############################
        // TopBar
        // ##############################
        titleBar.btnSelectAll.setOnClickListener {
            // If not all selected, select all
            if (!DataTracker.allSelected()) {
                // Change icon to opposite icon (deselect all)
                titleBar.btnSelectAll.setImageResource(R.drawable.ic_select_all_off)

                // Toggle all to selected state
                taskGroupAdapter.toggleSelectAll()
                DataTracker.selectAll()
                updateSelectedCountDisplay()

                // Switch to select mode if in add mode
                setMode(Mode.SELECTION)
            }
            // All selected, deselect all and return to add mode
            else {
                // Toggle all to off state and return to add mode
                taskGroupAdapter.toggleSelectAll(false)
                setMode(Mode.ADD)
            }
        }
        titleBar.btnCollapseExpand.setOnClickListener {
            // Expand all when all are collapsed, switch icon to collapse all icon
            if (DataTracker.allCollapsed()) {
                taskGroupAdapter.toggleFoldAll()
                toggleFoldIcon(Fold.OUT)
            }
            // Otherwise collapse all and switch icon to expand all icon
            else {
                taskGroupAdapter.toggleFoldAll(Fold.IN)
                toggleFoldIcon(Fold.IN)
            }
            updateSave()
        }
        titleBar.btnToggleLayout.setOnClickListener {
            Settings.setLayout()
            saveLoad.saveLayout()
            toggleLayoutButton()
        }
        // btnSettings.setOnClickListener { }

        // ##############################
        // BottomBar
        // ##############################
        // 1. Add Mode
        addMode.btnNewTask.setOnClickListener {
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
            addMode.txtTaskDesc.clearFocus()
            newTask.time.clear(addMode.txtSetTime)

            // Save changes
            updateSave()
        }

        addMode.txtSetDate.setOnClickListener { PopupManager.dateEdit(addMode, addMode.txtSetDate, this, newDate) }
        addMode.txtSetTime.setOnClickListener { PopupManager.timeEdit(addMode, addMode.txtSetTime, this, newTask.time) }
        addMode.btnSetTag.setOnClickListener  { PopupManager.tagEdit(addMode, addMode.btnSetTag, this, newTask) }

        addMode.btnReset.setOnClickListener {
            // Reset all values (exclude text entry)
            newTask.tag = R.drawable.tag_base
            newDate = today()
            newTask.time.clear()

            // Update views
            addMode.btnSetTag.setImageResource(newTask.tag)
            addMode.txtSetTime.text = defaultTimeMsg
            addMode.txtSetDate.text = newDate.asStringShort()
        }

        // 2. Select Mode
        selectMode.btnToDate.setOnClickListener {
            // 1. Create temporary Task to hold new date
            // 2. Create window, user selects new date
            // 3. Override date for selected tasks in adapter

            val newDate = TaskDate(-1)
            val window: PopupWindow = PopupManager.dateEdit(selectMode, null, this, newDate)
            window.setOnDismissListener {
                // Apply changes to selected date when window closed. If -1 then no date was selected
                if (newDate.id != -1) {
                    taskGroupAdapter.selectedSetDate(newDate)
                    setMode(Mode.ADD)
                    updateSave()
                }
            }
        }
        selectMode.btnToTime.setOnClickListener {
            val newTime = TaskTime(0)
            val window: PopupWindow = PopupManager.timeEdit(selectMode, null, this, newTime)
            window.setOnDismissListener {
                if (newTime.hour != 0 || newTime.duration != 0) {
                    taskGroupAdapter.selectedSetTime(newTime)
                    setMode(Mode.ADD)
                    updateSave()
                }
            }
        }
        selectMode.btnToTag.setOnClickListener {
            val newTag = Task("", -1)
            val window: PopupWindow = PopupManager.tagEdit(selectMode, null, this, newTag)
            window.setOnDismissListener {
                if (newTag.tag != -1) {
                    taskGroupAdapter.selectedSetTag(newTag.tag)
                    setMode(Mode.ADD)
                    updateSave()
                }
            }
        }

        selectMode.btnDelete.setOnClickListener {
            taskGroupAdapter.delete()
            setMode(Mode.ADD)
            updateSave()
        }
    }

    // ########## OnClick ##########
    private fun groupClicked(groupNum: Int) {
        val difference: Int = taskGroupAdapter.toggleGroupSelected(groupNum)
        val selectedPreClick = DataTracker.numSelected
        DataTracker.numSelected += difference

        when {
            // [1]. From 0 -> x selected. Enter select mode
            selectedPreClick == 0 -> {
                setMode(Mode.SELECTION)
            }
            // [2]. From x -> 0 selected. Return to add mode
            DataTracker.numSelected == 0 -> {
                setMode(Mode.ADD)
            }
            // [3]. From x -> x + y OR x -> x - y. Update value display
            else -> { updateSelectedCountDisplay() }
        }
    }
    private fun taskClicked (task: Task) {
        // Update counts based on whether task selected/deselected
        if (task.selected) {
            DataTracker.numSelected++
            // Selected 0 -> 1, change to selection mode. Otherwise update as usual
            if (DataTracker.numSelected == 1)
                setMode(Mode.SELECTION)
            else {
                updateSelectedCountDisplay()
                // If all selected, change topBar icon (selectAll to off)
                if (DataTracker.allSelected())
                    titleBar.btnSelectAll.setImageResource(R.drawable.ic_select_all_off)
            }
        }
        else {
            DataTracker.numSelected--
            // Selected tasks 1 -> 0, return to add mode. Otherwise update as usual
            if (DataTracker.numSelected == 0)
                setMode(Mode.ADD)
            else {
                updateSelectedCountDisplay()
                // If went from max to max - 1, change topBar icon (selectAll to on)
                if (DataTracker.numSelected == DataTracker.taskCount - 1)
                    titleBar.btnSelectAll.setImageResource(R.drawable.ic_select_all_on)
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
                DataTracker.numSelected = 0
                updateTopBar(mainTitle)

                // Switch display of bottomBar
                addMode.visibility = View.VISIBLE
                selectMode.visibility = View.GONE

                // Reset icon: Select all
                titleBar.btnSelectAll.setImageResource(R.drawable.ic_select_all_on)
            }
            Mode.SELECTION -> {
                updateSelectedCountDisplay()

                // Switch display of bottomBar
                addMode.visibility = View.GONE
                selectMode.visibility = View.VISIBLE
            }
            else -> return
        }
    }

    private fun updateTopBar(newTitle: String) { titleBar.title.text = newTitle }
    private fun updateSelectedCountDisplay() { updateTopBar("Selected: ${DataTracker.numSelected}") }

    // ########## Toggle ##########
    private fun toggleFoldIcon(state: Fold) {
        when(state) {
            Fold.OUT -> titleBar.btnCollapseExpand.setImageResource(R.drawable.ic_view_collapse)
            Fold.IN -> titleBar.btnCollapseExpand.setImageResource(R.drawable.ic_view_expand)
        }
    }
    private fun toggleLayoutButton() {
        when (Settings.mainLayout) {
            ViewLayout.LINEAR -> { titleBar.btnToggleLayout.setImageResource(R.drawable.ic_layout_linear) }
            ViewLayout.GRID -> { titleBar.btnToggleLayout.setImageResource(R.drawable.ic_layout_grid) }
        }
    }

    // ########## Save/Load ##########
    private fun loadSave() {
        saveLoad = SaveLoad(this)

        // Uncomment for broken data
        // saveLoad.clearAllData()

        // Load data
        taskGroupList = saveLoad.loadTaskGroupList()

        // Load settings
        Settings.mainLayout = saveLoad.loadLayout()
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
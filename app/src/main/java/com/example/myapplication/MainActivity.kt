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
// import com.example.myapplication.popup_windows.createDatePopup
import kotlinx.android.synthetic.main.main_activity_view.*
import kotlinx.android.synthetic.main.main_activity_view.btnReset
import kotlinx.android.synthetic.main.main_activity_view.topBarTitle
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    // Settings
    // private val settings: Settings = Settings()

    // TaskList (Center Area)
    private val taskClickedFn = { task : Task -> taskClicked(task) }
    private val dateClickedFn = { group: Int -> groupClicked(group) }
    private val toTopFn = { group: Int -> scrollTo(group) }
    private val updateCollapseExpandIconFn = { state: Fold -> updateCollapseExpandIcon(state)}
    private val updateSaveFn = { updateSave() }

    // Selecting tasks
    private var mode: Mode = Mode.START

    // Created task
    private var newTask: Task = Task()
    private var newDate: TaskDate = today()
    private val today: TaskDate = today()

    // Data
    private var data: TaskListData = TaskListData()
    private var taskGroupList: ArrayList<TaskGroup> = ArrayList()
    private var tagsList: ArrayList<Int> = ArrayList()

    // Late initialized variables
    private lateinit var saveLoad: SaveLoad
    private lateinit var taskGroupAdapter: TaskGroupAdapter

    // ########## Main ##########
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_view)

        // Check for existing saved data, attempt to load it then create the adapter
        loadSave()

        // Assign layout manager and adapter to recycler view
        dateGroupRV.apply {
            layoutManager = LinearLayoutManager(this.context)
            addItemDecoration(TaskListDecoration(1, Settings.groupSpacing, true, 0))
            // layoutManager = GridLayoutManager(this.context, 2, GridLayoutManager.VERTICAL, false)
            // addItemDecoration(TaskListDecoration(2, Settings.groupSpacing, true, 0))
            adapter = taskGroupAdapter
        }

        // Divider between date categories
        // dateGroupRV.addDivider()

        runSetup()
        setMode(Mode.ADD)
    }

    // ########## Setup related ##########
    private fun runSetup() {
        // Setup singletons
        Keyboard.setup(this, txtTaskDesc)
        Keyboard.addInputValidation(btnNewTask)

        // ToDo: Implement tags list to be saved and loaded
        tagsList = arrayListOf(
            R.drawable.tag_base,
            R.drawable.tag_booking,
            R.drawable.tag_buy,
            R.drawable.tag_event,
            R.drawable.tag_flight,
            R.drawable.tag_food,
            R.drawable.tag_medicine,
            R.drawable.tag_movies
        )
        PopupManager.setup(tagsList)

        // Initialize variable references
        // Apply starting date to be today's date at bottom bar
        txtSetDate.text = today.createShortLabel()
        // Set time to be blank
        newTask.time.clear(txtSetTime)

        // Buttons (topBar and bottomBar)
        setupButtons()
    }

    // ########## Buttons ##########
    private fun setupButtons() {
        // ##############################
        // TopBar
        // ##############################
        btnSelectAll.setOnClickListener {
            // If not all selected, select all
            if (!data.allSelected()) {
                // Change icon to opposite icon (deselect all)
                btnSelectAll.setImageResource(R.drawable.ic_select_all_off)

                // Toggle all to selected state
                taskGroupAdapter.toggleSelectAll()
                data.selectAll()
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
        btnCollapseExpand.setOnClickListener {
            // Expand all when all are collapsed, switch icon to collapse all icon
            if (taskGroupAdapter.allCollapsed()) {
                taskGroupAdapter.toggleFoldAll()
                updateCollapseExpandIcon(Fold.OUT)
            }
            // Otherwise collapse all and switch icon to expand all icon
            else {
                taskGroupAdapter.toggleFoldAll(Fold.IN)
                updateCollapseExpandIcon(Fold.IN)
            }
            updateSave()
        }
        btnSettings.setOnClickListener { }

        // ##############################
        // BottomBar
        // ##############################

        // Add mode
        btnNewTask.setOnClickListener {
            // Get relevant values
            val desc: String = txtTaskDesc.text.toString().trim()
            val time: TaskTime = newTask.time.copy()
            val date: TaskDate = newDate.copy()
            val tag: Int = newTask.tag
            val addedTask = Task(desc, tag, time)

            // Add new task to adapter
            taskGroupAdapter.addTask(date, addedTask)

            // Reset text entry and time
            txtTaskDesc.setText("")
            txtTaskDesc.clearFocus()
            newTask.time.clear(txtSetTime)

            // Save changes
            updateSave()
        }
        btnReset.setOnClickListener {
            // Reset all values (exclude text entry)
            newTask.tag = R.drawable.tag_base
            newDate = today()
            newTask.time.clear()

            // Update views
            btnSetTag.setImageResource(newTask.tag)
            txtSetTime.text = defaultTimeMsg
            txtSetDate.text = newDate.createShortLabel()
        }

        txtSetDate.setOnClickListener { PopupManager.dateEdit(addModeBar, txtSetDate, this, newDate) }
        txtSetTime.setOnClickListener { PopupManager.timeEdit(addModeBar, txtSetTime, this, newTask) }
        btnSetTag.setOnClickListener  { PopupManager.tagEdit(addModeBar, btnSetTag, this, newTask) }

        // Select mode
        // When modifying entries, clear selections and then return to add mode
        btnToDate.setOnClickListener {
            // See below for logic explanation
            val newDate = TaskDate(-1)
            val window: PopupWindow = PopupManager.dateEdit(selectModeBar, null, this, newDate)
            window.setOnDismissListener {
                if (newDate.id != -1) {
                    taskGroupAdapter.changeDateForSelected(newDate)
                    setMode(Mode.ADD)
                }
            }
        }
        btnToTag.setOnClickListener {
            // 1. Create temporary Task to hold new tag
            // 2. Create window, user selects new tag
            // 3. Override tag for selected tasks in adapter

            val newTag = Task("", -1)
            val window: PopupWindow = PopupManager.tagEdit(selectModeBar, null, this, newTag)
            window.setOnDismissListener {
                // Apply changes to selected tag when window closed. If -1 then no tag was selected
                if (newTag.tag != -1) {
                    taskGroupAdapter.setTagForSelected(newTag.tag)
                    setMode(Mode.ADD)
                }
            }
        }

        btnClearTag.setOnClickListener {
            taskGroupAdapter.clearSelected(TaskParam.Tag)
            setMode(Mode.ADD)
            updateSave()
        }
        btnClearTime.setOnClickListener {
            taskGroupAdapter.clearSelected(TaskParam.Time)
            setMode(Mode.ADD)
            updateSave()
        }
        btnDelete.setOnClickListener {
            taskGroupAdapter.delete()
            setMode(Mode.ADD)
            updateSave()
        }
    }

    // ########## OnClick ##########
    private fun groupClicked(groupNum: Int) {
        val difference: Int = taskGroupAdapter.toggleGroupSelected(groupNum)
        val selectedPreClick = data.numSelected
        data.numSelected += difference

        when {
            // [1]. From 0 -> x selected. Enter select mode
            selectedPreClick == 0 -> {
                setMode(Mode.SELECTION)
            }
            // [2]. From x -> 0 selected. Return to add mode
            data.numSelected == 0 -> {
                setMode(Mode.ADD)
            }
            // [3]. From x -> x + y OR x -> x - y. Update value display
            else -> { updateSelectedCountDisplay() }
        }
    }

    private fun taskClicked (task: Task) {
        // Update counts based on whether task selected/deselected
        if (task.selected) {
            data.numSelected++
            // Selected 0 -> 1, change to selection mode. Otherwise update as usual
            if (data.numSelected == 1)
                setMode(Mode.SELECTION)
            else {
                updateSelectedCountDisplay()
                // If all selected, change topBar icon (selectAll to off)
                if (data.allSelected())
                    btnSelectAll.setImageResource(R.drawable.ic_select_all_off)
            }
        }
        else {
            data.numSelected--
            // Selected tasks 1 -> 0, return to add mode. Otherwise update as usual
            if (data.numSelected == 0)
                setMode(Mode.ADD)
            else {
                updateSelectedCountDisplay()
                // If went from max to max - 1, change topBar icon (selectAll to on)
                if (data.numSelected == data.taskCount - 1)
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
                data.numSelected = 0
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
    private fun updateSelectedCountDisplay() { updateTopBar("Selected: ${data.numSelected}") }

    private fun updateCollapseExpandIcon(state: Fold) {
        when(state) {
            Fold.OUT -> btnCollapseExpand.setImageResource(R.drawable.ic_view_collapse)
            Fold.IN -> btnCollapseExpand.setImageResource(R.drawable.ic_view_expand)
        }
    }

    // ########## Save/Load ##########
    private fun loadSave() {
        saveLoad = SaveLoad(this)
        // saveLoad.clearAllData()
        taskGroupList = saveLoad.loadTaskGroupList()
        // settings = saveLoad.loadSettings()
        taskGroupAdapter =
            TaskGroupAdapter(data, taskGroupList,
                taskClickedFn, dateClickedFn, toTopFn,
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
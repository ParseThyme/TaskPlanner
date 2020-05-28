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
    private var taskGroupList: ArrayList<GroupEntry> = ArrayList()
    private var tagsList: ArrayList<Int> = ArrayList()

    // Late initialized variables
    private lateinit var saveData: SaveData
    private lateinit var taskGroupAdapter: TaskGroupAdapter

    // Modified tasks
    private var selectModeDate: TaskDate = today()

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

        // Initialize variable references
        // Apply starting date to be today's date at bottom bar
        addMode.txtSetDate.text = Settings.today.asStringShort()
        // Set time to be blank
        newTask.time.unset()
        addMode.txtSetTime.text = defaultTimeMsg

        // Buttons (topBar and bottomBar)
        setupButtons()

        // Main App name
        titleBar.title.text = mainTitle
    }

    // ########## Buttons ##########
    private fun setupButtons() {
        // ##############################
        // TopBar
        // ##############################
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
            saveData.saveLayout()
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
            // newTask.time.clear(addMode.txtSetTime)
            newTask.time.unset()
            addMode.txtSetTime.text = defaultTimeMsg

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
            // newTask.time.clear()
            newTask.time.unset()
            addMode.txtSetTime.text = defaultTimeMsg

            // Update views
            addMode.btnSetTag.setImageResource(newTask.tag)
            addMode.txtSetTime.text = defaultTimeMsg
            addMode.txtSetDate.text = newDate.asStringShort()
        }

        // 2. Select Mode
        selectMode.btnSelectAll.setOnClickListener {
            when (DataTracker.allSelected()) {
                // Not all selected, select all
                false -> {
                    taskGroupAdapter.toggleSelectAll()
                    DataTracker.selectAll()
                    updateSelectedCountDisplay()
                }

                // Deselect all except for initially selected task
                true -> {

                }
            }
        }
        selectMode.btnSelectNone.setOnClickListener {
            // Toggle all to off state and return to add mode
            taskGroupAdapter.toggleSelectAll(false)
            setMode(Mode.ADD)
        }

        selectMode.btnToDate.setOnClickListener {
            // 1. Create temporary Task to hold new date
            // 2. Create window, user selects new date
            // 3. Override date for selected tasks in adapter
            selectModeDate.id = -1
            val window: PopupWindow = PopupManager.dateEdit(selectMode, null, this, selectModeDate)
            window.setOnDismissListener {
                // Apply changes to selected date when window closed. If -1 then apply tick wasn't pressed
                if (selectModeDate.id != -1) {
                    taskGroupAdapter.selectedSetDate(selectModeDate)
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
        selectMode.btnClearParams.setOnClickListener {
            taskGroupAdapter.selectedClearAll()
            setMode(Mode.ADD)
            updateSave()
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
        when (task.selected) {
            true -> {
                DataTracker.numSelected++
                when (DataTracker.numSelected) {
                    1 -> setMode(Mode.SELECTION)            // 0 -> 1. Enter selectionMode
                    else -> updateSelectedCountDisplay()    // Update count display
                }
            }

            false -> {
                DataTracker.numSelected--
                when (DataTracker.numSelected) {
                    0 -> setMode(Mode.ADD)                  // 1 -> 0. Return to addMode
                    else -> updateSelectedCountDisplay()    // Update count display
                }
            }
        }
    }

    // ########## Change values/display ##########
    private fun setMode(newMode: Mode) {
        // Do nothing if called on same mode
        if (mode == newMode) return

        mode = newMode
        when (newMode) {
            Mode.ADD -> {
                // Set none selected and show main title
                DataTracker.numSelected = 0

                // Switch display of bottomBar
                addMode.visibility = View.VISIBLE
                selectMode.visibility = View.GONE
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
    private fun updateSelectedCountDisplay() { selectMode.txtSelected.text = DataTracker.numSelectedMsg() }

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
        saveData = SaveData(this)

        // Uncomment for broken data
        // saveLoad.clearAllData()

        // Load tagsList. If not present, create new list
        tagsList = saveData.loadTagsList()
        if (tagsList.isEmpty()) {
            tagsList = arrayListOf(
                R.drawable.tag_booking, R.drawable.tag_assignment, R.drawable.tag_mail, R.drawable.tag_file,
                R.drawable.tag_scan, R.drawable.tag_print, R.drawable.tag_bug, R.drawable.tag_build,

                R.drawable.tag_tv ,R.drawable.tag_read, R.drawable.tag_music_note, R.drawable.tag_game,
                R.drawable.tag_photo, R.drawable.tag_movie, R.drawable.tag_food, R.drawable.tag_event,

                R.drawable.tag_buy, R.drawable.tag_pet, R.drawable.tag_workout, R.drawable.tag_medicine,
                R.drawable.tag_delivery, R.drawable.tag_flight, R.drawable.tag_train, R.drawable.tag_car,

                R.drawable.tag_important, R.drawable.tag_flag, R.drawable.tag_1, R.drawable.tag_2,
                R.drawable.tag_3, R.drawable.tag_4, R.drawable.tag_5, R.drawable.tag_6
            )
            saveData.saveTagsList(tagsList)
        }
        PopupManager.setup(tagsList)

        // Load data
        taskGroupList = saveData.loadTaskGroupList()

        // Load settings
        Settings.mainLayout = saveData.loadLayout()
    }
    private fun updateSave() { saveData.saveTaskGroupList(taskGroupList) }

    // ########## Utility ##########
    // Scroll to position when group opened/closed (accounts for opening/closing top/bottom)
    private fun scrollTo(position: Int) {
        if (Settings.usingGridLayout()) return

        dateGroupRV.scrollToPosition(position)
        // Scroll bit extra for last position
        if (position == taskGroupList.lastIndex) {
            (dateGroupRV.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 20)
        }
    }
}
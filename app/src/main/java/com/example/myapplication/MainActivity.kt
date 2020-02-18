package com.example.myapplication

import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.TaskGroupAdapter
import com.example.myapplication.data_classes.*
import com.example.myapplication.popup_windows.*
import com.example.myapplication.popup_windows.edit.PopupEdit
// import com.example.myapplication.popup_windows.createDatePopup
import kotlinx.android.synthetic.main.main_view.*
import kotlinx.android.synthetic.main.popup_time.view.*
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
    private val updateCollapseExpandIconFn = { state: ViewState -> updateCollapseExpandIcon(state)}
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

    // Time allocated to task
    private var time: TaskTime = TaskTime()

    // Created task
    private var date: String = ""

    // Popups
    private val viewSizeFn = { getViewDimensions() }
    private lateinit var datePopup: PopupDateOld
    private lateinit var timePopup: PopupTime
    private lateinit var tagPopup: PopupTag

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

        val popupEdit = PopupEdit(btnTest, this)
        btnTest.setOnClickListener {
            popupEdit.create()
        }

        runSetup()
        setMode(Mode.ADD)
    }

    // ########## Setup related ##########
    private fun runSetup() {
        setDefaultValues()

        // Initialize variable references
        setupLateInit()

        // Buttons (topBar and bottomBar)
        setupButtons()
    }

    private fun setupLateInit() {
        // Add new task variables
        val cal = Calendar.getInstance()
        // Apply starting date to be today's date at bottom bar
        btnSetDate.text = createDateLabel(cal, true)

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

        // When text changed, check for non-empty input
        txtTaskDesc.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable) { }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            // Check when text is being changed
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Toggle confirm button based on whether text is empty or not
                btnNewTask.isEnabled = txtTaskDesc.text.isNotEmpty()
                btnNewTask.toggle(btnNewTask.isEnabled)
            }
        })
        // When pressing out of taskDesc, close the keyboard
        txtTaskDesc.closeKeyboardOnFocusLost()

        // Settings
        // taskDesc.setMaxLength(settings.taskMaxLength)
    }

    private fun setDefaultValues() {
        btnSetTime.text = defaultTimeMsg
    }

    // ########## Buttons ##########
    private fun setupButtons() {
        datePopup = PopupDateOld(btnSetDate, settings, this)
        timePopup = PopupTime(btnSetTime, this)
        tagPopup = PopupTag(btnSetTag, this)

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
        btnSetDate.setOnClickListener { datePopup.create() }
        btnNewTask.setOnClickListener { newTaskBtnFn() }
        btnReset.setOnClickListener { resetBtnFn() }
        btnSetTime.setOnClickListener { timePopupFn() }
        btnSetTag.setOnClickListener { tagPopup.create() }

        // Select mode
        btnDelete.setOnClickListener { deleteBtnFn() }
    }

    private fun newTaskBtnFn() {
        // Get relevant values
        val desc: String = txtTaskDesc.text.toString().trim()

        val tag: Tag = tagPopup.selectedTag
        val date: TaskDate = datePopup.selectedDate

        val newTask = Task(desc, tag, time)

        // Add new task to adapter
        taskGroupAdapter.addTask(date, newTask)

        // Reset values
        txtTaskDesc.setText("")
        txtTaskDesc.clearFocus()
        txtTaskDesc.hideKeyboard()

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

    private fun timePopupFn() {
        val window:PopupWindow = timePopup.create()
        val view: View = window.contentView

        // Use currently selected times for time and duration
        view.txtHour.text = time.hour.toString()
        view.txtMinute.text = minutesAsString(time.min)
        view.txtTimeOfDay.text = time.timeOfDay

        view.txtDuration.text = durationAsString(time.duration)
        view.txtIncDelta.text = durationAsString(time.durationInc)

        // Save updated time when window closed
        view.btnApplyTime.setOnClickListener {
            window.dismiss()
            val hour:Int = view.txtHour.text.toString().toInt()
            val minutes:Int = view.txtMinute.text.toString().toInt()
            val timeOfDay:String = view.txtTimeOfDay.text.toString()
            val duration:Int = durationAsInt(view.txtDuration.text.toString())
            val durationInc: Int = durationAsInt(view.txtIncDelta.text.toString())

            // Assign time value
            time = TaskTime(hour, minutes, timeOfDay, duration, durationInc)

            // Generate displayed string
            var display = time.createDisplayedTime()

            // Clear entry if its hour was "0" and set message to display as default
            if (!time.isValid()) {
                time.resetValues()
                display = defaultTimeMsg
            }

            // Display time on button
            btnSetTime.text = display
        }
    }

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
        taskGroupList = saveLoad.loadTaskGroupList()
        // settings = saveLoad.loadSettings()
        taskGroupAdapter = TaskGroupAdapter(taskGroupList, settings,
            taskClickedFn, dateClickedFn, toTopFn, updateCollapseExpandIconFn, updateSaveFn)
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
            (dateGroupRV.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 20)
        }
    }
    private fun ImageButton.toggle(enabled: Boolean) {
        if (enabled) updateBtnColor(R.color.btnEnabled, applicationContext)
        else updateBtnColor(R.color.btnDisabled, applicationContext)
    }
    // Sizing of current main layout (adjusts depending of factors affecting size, e.g. Opening keyboard)
    private fun getViewDimensions(): Point { return Point(mainLayout.width, mainLayout.height) }
}

enum class Mode { START, ADD, SELECTION }
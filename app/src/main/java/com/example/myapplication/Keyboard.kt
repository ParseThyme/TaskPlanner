package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import kotlinx.android.synthetic.main.main_activity_view.*

object Keyboard {
    var visible: Boolean = false
    var height: Int  = 0
    var maxHeight: Int = 0
    var width: Int = 0

    // Current editText that Keyboard is attached to
    lateinit var editText: EditText
    private lateinit var imm: InputMethodManager

    fun setup(activity: MainActivity, startingText: EditText) {
        val root = activity.window.decorView

        // Initial editText that keyboard is attached to
        attachTo(startingText)

        // Size of view when keyboard is closed. Basically maximum height it reaches and width (shouldn't change)
        val occupiedSpace = root.getOccupiedSpace()
        maxHeight = occupiedSpace.y
        width = occupiedSpace.x + 10                            // Balance out padding

        // https://proandroiddev.com/how-to-detect-if-the-android-keyboard-is-open-269b255a90f5
        root.viewTreeObserver.addOnGlobalLayoutListener {
            // How much space is being occupied currently. Same as maxHeight if keyboard is not open
            val occupiedSpace = root.getOccupiedSpace()

            // Size of keyboard equal to max height - occupied space. If 0 we know keyboard is not open.
            height = maxHeight - occupiedSpace.y

            // Keyboard size is generally over 200. If value is less then keyboard isn't open
            visible = height > 200
        }
    }

    fun attachTo(text: EditText) {
        editText = text
        imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // Focus changed listener. When focus lost on textView, close keyboard
        editText.setOnFocusChangeListener { _, focused ->
            if (!focused) { close() }
        }
    }

    fun addInputValidation(toggledButton: ImageButton) {
        // Input Validation:
        // TextWatcher. Ensure button only enabled when text entered (can't submit blank entries)
        toggledButton.isEnabled = false
        toggledButton.toggle(false)

        // When text changed, check for non-empty input
        editText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable) { }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            // Check when text is being changed
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Toggle confirm button based on whether text is empty or not
                toggledButton.isEnabled = editText.text.isNotEmpty()
                toggledButton.toggle(toggledButton.isEnabled)
            }
        })
    }

    // Manually force close keyboard, assuming it is open. Called when a dialog window itself is closed
    fun close() { if (visible) hide() }

    // https://support.honeywellaidc.com/s/article/Android-with-hardware-keyboard-force-show-hide-Soft-Keyboard-on-EditText
    private fun hide() { imm.hideSoftInputFromWindow(editText.windowToken, 0) }
    private fun show() {
        visible = true
        imm.toggleSoftInputFromWindow(editText.windowToken, InputMethodManager.SHOW_FORCED, 0)
    }
}

private fun ImageButton.toggle(enabled: Boolean) {
    if (enabled) updateBtnColor(R.color.btnEnabled, this.context)
    else updateBtnColor(R.color.btnDisabled, this.context)
}

private fun View.getOccupiedSpace() : Point {
    // How much space currently the view takes up
    // https://stackoverflow.com/questions/22589322/what-does-top-left-right-and-bottom-mean-in-android-rect-object
    val r = Rect()  // Has top, bottom, left, right values. We only care about right/bottom in this case
    this.getWindowVisibleDisplayFrame(r)
    return Point(r.right, r.bottom)
}
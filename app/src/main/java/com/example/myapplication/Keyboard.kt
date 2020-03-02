package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.*


class Keyboard(private val editText: EditText) {
    private var visible: Boolean = false
    private val imm: InputMethodManager = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    init {
        // Keyboard guaranteed to open which editText clicked on, and so set visible to true
        editText.setOnClickListener { if (!visible) visible = true }
        // https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
        // Focus changed listener. When focus lost on textView, close keyboard
        editText.setOnFocusChangeListener { _, focused ->
            if (!focused) { close() }
        }
    }

    private fun toggle() {
        // Hide keyboard
        if (visible) {
            visible = false
            hide()
        }
        // Show keyboard
        else { show() }
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

    // Manually force close keyboard, assuming it is open. Called when dialog window itself is closed
    fun close() { if (visible) hide() }

    // https://support.honeywellaidc.com/s/article/Android-with-hardware-keyboard-force-show-hide-Soft-Keyboard-on-EditText
    private fun hide() { imm.hideSoftInputFromWindow(editText.windowToken, 0) }
    private fun show() {
        visible = true
        imm.toggleSoftInputFromWindow(editText.windowToken, InputMethodManager.SHOW_FORCED, 0)
    }

    private fun ImageButton.toggle(enabled: Boolean) {
        if (enabled) updateBtnColor(R.color.btnEnabled, this.context)
        else updateBtnColor(R.color.btnDisabled, this.context)
    }
}
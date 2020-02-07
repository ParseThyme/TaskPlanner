package com.example.myapplication

import android.content.Context
import android.text.InputFilter.LengthFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.settings_alertdialog.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_MONTH

// ########## Keyboard ##########
// https://support.honeywellaidc.com/s/article/Android-with-hardware-keyboard-force-show-hide-Soft-Keyboard-on-EditText
fun View.hideKeyboard() {
    val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

// https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
fun View.closeKeyboardOnFocusLost() {
    this.setOnFocusChangeListener { _, focused ->
        // Hide keyboard when user presses out of taskDesc
        if (!focused)
            this.hideKeyboard()
    }
}

// ########## Creation ##########
fun RecyclerView.addDivider(vertical : Boolean = true) {
    // Divider between date categories
    var orientation = DividerItemDecoration.VERTICAL
    if (!vertical) { orientation = DividerItemDecoration.HORIZONTAL }

    val divider = DividerItemDecoration(this.context, orientation)
    this.addItemDecoration(divider)
}

// ########## Date ##########
fun createDateLabel(cal: Calendar, short: Boolean = false) : String{
    val timeInMills = cal.timeInMillis
    // Produce day, generally either in Monday or Mon format. We want only two characters (Mo, Tu, We, etc)
    val dayName: String
    val month: String
    val day: String = dayFormat.format(timeInMills)
    var result: String

    if (short) {
        dayName = SimpleDateFormat("EE").format(timeInMills).dropLast(1)
        month = SimpleDateFormat("MMM").format(timeInMills)
        result = "$dayName $month $day"
    } else {
        dayName = SimpleDateFormat("EEEE").format(timeInMills)
        month = SimpleDateFormat("MMMM").format(timeInMills)

        // Depending on day, add ordinals
        // https://stackoverflow.com/questions/4011075/how-do-you-format-the-day-of-the-month-to-say-11th-21st-or-23rd-ordinal
        val ordinal = addOrdinal(cal.get(DAY_OF_MONTH))
        result = "$dayName $month $day$ordinal"
    }

    return result
}

private fun addOrdinal(dayNum: Int) : String {
    // Set ordinal for 11th, 12th, 13th unique cases
    return if (dayNum in 11..13) {
        "th"
    }
    // Otherwise if ending with 1 == st, 2 == nd, 3 == rd, 4-9 == th
    else {
        when (dayNum % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
}

// ########## Shortcuts ##########
// https://stackoverflow.com/questions/2461824/how-to-programmatically-set-maxlength-in-android-textview
// fun EditText.setMaxLength(length: Int) { this.filters = arrayOf(LengthFilter(length)) }

fun ImageButton.updateBtnColor(color: Int, context: Context) {
    this.setColorFilter(ContextCompat.getColor(context, color))
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

// ########## Other ##########
// https://stackoverflow.com/questions/33381384/how-to-use-typetoken-generics-with-gson-in-kotlin
inline fun <reified T> Gson.fromJson(json: String?): T = this.fromJson<T>(json, object: TypeToken<T>() {}.type)

/** ########## Tutorials: ##########
 - Add Item: https://blog.stylingandroid.com/recyclerview-animations-add-remove-items/
 - OnClick: https://stackoverflow.com/questions/54219825/android-kotlin-how-to-add-click-listener-to-recyclerview-adapter
 - Nested RecyclerView: https://android.jlelse.eu/easily-adding-nested-recycler-view-in-android-a7e9f7f04047
 - RecyclerView: https://www.youtube.com/watch?v=jS0buQyfJfs
 - Popup Dialog box: https://www.youtube.com/watch?v=2Nj6qCtaUqw
 - Disabling button/enabling based on text field: https://www.youtube.com/watch?v=Vy_4sZ6JVHM
 - Bottom Navigation: https://android--code.blogspot.com/2018/03/android-kotlin-bottom-navigation-bar.html
 - Saving data (sharedPreferences): http://www.kotlincodes.com/kotlin/shared-preferences-with-kotlin/
 - Hide Keyboard: https://stackoverflow.com/questions/41790357/close-hide-the-android-soft-keyboard-with-kotlin
 - Show Keyboard: http://www.androidtutorialshub.com/how-to-hide-and-show-soft-keyboard-in-android/
 - Function parameter: https://stackoverflow.com/questions/41887584/passing-function-as-parameter-in-kotlin
 - Toggle Image: https://stackoverflow.com/questions/11499574/toggle-button-using-two-image-on-different-state/11499595#11499595
 - Change Activity: https://www.tutorialkart.com/kotlin-android/android-start-another-activity/
 - Pass Data between activities: https://devofandroid.blogspot.com/2018/03/pass-data-between-activities-using.html
 **/

/** ########## Notes: ##########
 - ? == ignore if null return/match
 - Unit == equivalent to void return type in Java
 **/
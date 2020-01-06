package com.example.myapplication

import android.content.Context
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import java.util.Calendar.DAY_OF_MONTH
import kotlin.collections.ArrayList


fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun View.hideKeyboard() {
 val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
 imm.hideSoftInputFromWindow(windowToken, 0)
}

fun RecyclerView.addDivider(vertical : Boolean = true) {
    // Divider between date categories
    var orientation = DividerItemDecoration.VERTICAL
    if (!vertical) { orientation = DividerItemDecoration.HORIZONTAL }

    val divider = DividerItemDecoration(this.context, orientation)
    this.addItemDecoration(divider)
}

// https://stackoverflow.com/questions/2461824/how-to-programmatically-set-maxlength-in-android-textview
fun EditText.setMaxLength(length: Int) { this.filters = arrayOf(LengthFilter(length)) }

// https://stackoverflow.com/questions/33381384/how-to-use-typetoken-generics-with-gson-in-kotlin
inline fun <reified T> Gson.fromJson(json: String?): T = this.fromJson<T>(json, object: TypeToken<T>() {}.type)

// ########## Date ##########
fun createDateLabel(cal: Calendar, short: Boolean = false) : String{
 val timeInMills = cal.timeInMillis
 // Produce day, generally either in Monday or Mon format. We want only two characters (Mo, Tu, We, etc)
 var dayName: String
 var month: String
 val day: String = dayFormat.format(timeInMills)

 if (short) {
   dayName = sdayNameFormat.format(timeInMills).dropLast(1)
   month = smonthFormat.format(timeInMills)
 } else {
   dayName = dayNameFormat.format(timeInMills)
   month = monthFormat.format(timeInMills)
 }

 // Depending on day, add ordinals
 // https://stackoverflow.com/questions/4011075/how-do-you-format-the-day-of-the-month-to-say-11th-21st-or-23rd-ordinal
 val ordinal = addOrdinal(cal.get(DAY_OF_MONTH))

 return "$dayName $month $day$ordinal"
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
 - Function parameter: https://stackoverflow.com/questions/41887584/passing-function-as-parameter-in-kotlin
 **/

/** ########## Notes: ##########
 - ? == ignore if null return/match
 - Unit == equivalent to void return type in Java
 **/
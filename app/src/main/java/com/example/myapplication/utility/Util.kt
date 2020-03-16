package com.example.myapplication.utility

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.main_activity_view.view.*

// ########## Hardcoded values (unmodified by app) ##########
// SharedPreferences (Saving/Loading data)
const val spName: String = "SavedData"
const val spTaskGroupList = "TaskGroupList"

// Activity.main
const val mainTitle = "Task Planner"
const val defaultTimeMsg = "Set Time"

/* Settings set as a singleton class, we only need one instance of it and we want to make it
 * globally accessible
 * https://blog.mindorks.com/how-to-create-a-singleton-class-in-kotlin
 */


// ########## Shortcuts ##########
// https://stackoverflow.com/questions/2461824/how-to-programmatically-set-maxlength-in-android-textview
// fun EditText.setMaxLength(length: Int) { this.filters = arrayOf(LengthFilter(length)) }

fun ImageButton.updateBtnColor(color: Int, context: Context) { setColorFilter(ContextCompat.getColor(context, color)) }

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

// ########## Other ##########
// https://stackoverflow.com/questions/33381384/how-to-use-typetoken-generics-with-gson-in-kotlin
inline fun <reified T> Gson.fromJson(json: String?): T = this.fromJson<T>(json, object: TypeToken<T>() {}.type)

/*
// https://stackoverflow.com/questions/35780980/getting-the-actual-screen-height-android/45158798
fun View.getDisplaySize() : Point {
    val root: View = this.rootView.layout
    return Point(root.width, root.height)
}
*/

fun View.applyBackgroundColor(color: String) { setBackgroundColor(Color.parseColor(color)) }
fun View.applyBackgroundColor(color: Int) { setBackgroundColor(color) }

fun printDebugMsg(message: String) {
   Log.d("Test", message)
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
 - Show Keyboard: http://www.androidtutorialshub.com/how-to-hide-and-show-soft-keyboard-in-android/
 - Function parameter: https://stackoverflow.com/questions/41887584/passing-function-as-parameter-in-kotlin
 - Toggle Image: https://stackoverflow.com/questions/11499574/toggle-button-using-two-image-on-different-state/11499595#11499595
 - Change Activity: https://www.tutorialkart.com/kotlin-android/android-start-another-activity/
 - Pass Data between activities: https://devofandroid.blogspot.com/2018/03/pass-data-between-activities-using.html
 - Radio Group RecyclerView: http://joshskeen.com/building-a-radiogroup-recyclerview/
 **/

/** ########## Notes: ##########
 - ? == ignore if null return/match
 - Unit == equivalent to void return type in Java
 **/
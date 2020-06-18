package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

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

fun ImageButton.updateBtnColor(color: Int, context: Context) {
    setColorFilter(ContextCompat.getColor(context, color))
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

// ########## Other ##########
fun View.applyBackgroundColor(color: String) {
    setBackgroundColor(Color.parseColor(color))
}

fun View.applyBackgroundColor(color: Int = Color.TRANSPARENT) {
    setBackgroundColor(color)
}

enum class Side { TOP, BOT, LEFT, RIGHT }


fun TextView.updateDrawableTop(resource: Int, tint: Int = R.color.iconTint) {
    updateDrawable(resource, Side.TOP, tint)
}
fun TextView.updateDrawableLeft(resource: Int, tint: Int = R.color.iconTint) {
    updateDrawable(resource, Side.LEFT, tint)
}
private fun TextView.updateDrawable(resource: Int, side: Side, tint: Int = R.color.iconTint) {
    val drawable: Drawable? = ContextCompat.getDrawable(context, resource)
    DrawableCompat.setTint(drawable!!, ContextCompat.getColor(context, tint))
    when (side) {
        Side.TOP -> setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
        Side.BOT -> setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable)
        Side.LEFT -> setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        Side.RIGHT -> setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    }
}

fun debugMessagePrint(message: String) {
    Log.d("Test", message)
}

fun millisecondsToDays(milliseconds: Long): Int {
    /* 1. /1000 == ms to s
     * 2. /60   == s to m
     * 3. /60   == m to h
     * 4. /24   == h to d
     */
    return (milliseconds / 1000 / 60 / 60 / 24).toInt()
}

// https://stackoverflow.com/questions/42308580/kotlin-extension-for-next-enum-value-without-reflection
/*
inline fun <reified T: Enum<T>> T.next(): T {
 val values = enumValues<T>()
 val nextOrdinal = (ordinal + 1) % values.size
 return values[nextOrdinal]
}
*/

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
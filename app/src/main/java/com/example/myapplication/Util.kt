package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

/** ########## Tutorials: ##########
 - Add Item: https://blog.stylingandroid.com/recyclerview-animations-add-remove-items/
 - OnClick: https://stackoverflow.com/questions/54219825/android-kotlin-how-to-add-click-listener-to-recyclerview-adapter
 - Nested RecyclerView: https://android.jlelse.eu/easily-adding-nested-recycler-view-in-android-a7e9f7f04047
 - RecyclerView: https://www.youtube.com/watch?v=jS0buQyfJfs
 - Popup Dialog box: https://www.youtube.com/watch?v=2Nj6qCtaUqw
 - Disabling button/enabling based on text field: https://www.youtube.com/watch?v=Vy_4sZ6JVHM
 - Bottom Navigation: https://android--code.blogspot.com/2018/03/android-kotlin-bottom-navigation-bar.html
 **/

/** ########## Notes: ##########
 - ? == ignore if null return/match
 - Unit == equivalent to void return type in Java
 **/
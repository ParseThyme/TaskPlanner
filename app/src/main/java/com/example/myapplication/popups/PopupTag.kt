package com.example.myapplication.popups

import android.R.attr.button
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapters.TaskTagAdapter
import com.example.myapplication.data_classes.Task
import com.example.myapplication.utility.Settings
import com.example.myapplication.utility.updateDrawableTop
import kotlinx.android.synthetic.main.popup_tag.view.*


class PopupTag(private val tagsList: ArrayList<Int>) : Popup() {
    fun create(attachTo: View, modify: Button?, context: Context, edited: Task): PopupWindow {
        // Calculate number of icons per row. Ideally max is 10 per row.
        var spanCount: Int = Settings.tagRowSize
        if (tagsList.size < spanCount)
            spanCount = tagsList.size

        val window: PopupWindow = create(context, R.layout.popup_tag)
        val view: View = window.contentView

        // Remove tag
        view.btnClearTag.setOnClickListener {
            modify?.updateDrawableTop(R.drawable.tag_base)
            edited.tag = R.drawable.tag_base
            window.dismiss()
        }
        // Change tag
        view.tagsRv.apply {
            layoutManager = GridLayoutManager(context, spanCount)
            adapter = TaskTagAdapter(tagsList)
            // Select and close function passed into TaskTagAdapter
            { chosenTag: Int ->           // Input Param
                modify?.updateDrawableTop(chosenTag)
                edited.tag = chosenTag
                window.dismiss()
            }
        }
        // Dismiss window
        view.tagDismissLeft.setOnClickListener { window.dismiss() }
        view.tagDismissRight.setOnClickListener { window.dismiss() }

        window.show(attachTo)
        return window
    }
}
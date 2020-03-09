package com.example.myapplication.popup_windows

import android.content.Context
import android.media.Image
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapters.TaskTagAdapter
import com.example.myapplication.data_classes.Task
import kotlinx.android.synthetic.main.popup_tag_grid_rv.view.*

class PopupTag(private val tagsList: ArrayList<Int>) : PopupParent() {
    fun create(attachTo: View, modify: View, context: Context, edited: Task, anchor: Anchor = Anchor.Above): PopupWindow {
        // Calculate number of icons per row. Ideally max is 10 per row.
        var spanCount = tagsList.size
        spanCount =
            if (tagsList.size < 8) tagsList.size
            else 8

        val window = create(context, R.layout.popup_tag_grid_rv)
        val view: View = window.contentView
        view.tagsRv.apply {
            layoutManager = GridLayoutManager(context, spanCount)
            adapter = TaskTagAdapter(tagsList)
            // Select and close function passed into TaskTagAdapter
            { taskTag: Int ->           // Input Param
                (modify as ImageView).setImageResource(taskTag)
                edited.tag = taskTag
                window.dismiss()
            }
        }

        window.show(attachTo)
        return window
    }
}
package com.example.myapplication.popups

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapters.TaskTagAdapter
import com.example.myapplication.data_classes.Task
import com.example.myapplication.utility.Settings
import kotlinx.android.synthetic.main.popup_tag.view.*

class PopupTag(private val tagsList: ArrayList<Int>) : Popup() {
    fun create(attachTo: View, modify: ImageView?, context: Context, edited: Task): PopupWindow {
        // Calculate number of icons per row. Ideally max is 10 per row.
        var spanCount: Int = Settings.tagRowSize
        if (tagsList.size < spanCount)
            spanCount = tagsList.size

        val window: PopupWindow = create(context, R.layout.popup_tag)
        val view: View = window.contentView

        // Change tag
        view.tagsRv.apply {
            layoutManager = GridLayoutManager(context, spanCount)
            adapter = TaskTagAdapter(tagsList)
            // Select and close function passed into TaskTagAdapter
            { chosenTag: Int ->           // Input Param
                modify?.setImageResource(chosenTag)
                edited.tag = chosenTag
                window.dismiss()
            }
        }

        // Remove tag
        view.btnClearTag.setOnClickListener {
            modify?.setImageResource(R.drawable.tag_base)
            edited.tag = R.drawable.tag_base
            window.dismiss()
        }

        window.show(attachTo)
        return window
    }
}
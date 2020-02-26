package com.example.myapplication.popup_windows

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import com.example.myapplication.R
import com.example.myapplication.data_classes.Task
import com.example.myapplication.data_classes.TaskTag
import com.example.myapplication.data_classes.setImageResourceFromTag
import kotlinx.android.synthetic.main.popup_tag.view.*

class PopupTag : PopupParent() {
    private var tag = TaskTag.NONE

    fun create(attachTo: View, modify: View, context: Context, edited: Task): PopupWindow {
        val window:PopupWindow = createAndShow(context, R.layout.popup_tag, attachTo)

        // Change tag displayed selecting appropriate tag from group
        tag = edited.tag
        window.contentView.tagGroup.setOnCheckedChangeListener { _, chosenTag ->
            tag = when (chosenTag) {
                R.id.tagEvent -> TaskTag.EVENT
                R.id.tagBooking -> TaskTag.BOOKING
                R.id.tagBuy -> TaskTag.BUY
                else -> TaskTag.NONE
            }

            (modify as ImageView).setImageResourceFromTag(tag)
            edited.tag = tag
            window.dismiss()
        }

        return window
    }
}
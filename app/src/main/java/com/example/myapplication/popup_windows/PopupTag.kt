package com.example.myapplication.popup_windows

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import com.example.myapplication.R
import com.example.myapplication.data_classes.Tag
import com.example.myapplication.data_classes.setImageResourceFromTag
import kotlinx.android.synthetic.main.tag_popup_window.view.*

class PopupTag(private val parent: ImageView, private val context: Context) : PopupWindowParent() {
    var selectedTag = Tag.NONE
    private set

    fun create(anchor: Anchor = Anchor.BottomLeft): PopupWindow {
        val window:PopupWindow = createAndShow(context, R.layout.tag_popup_window, parent, anchor)
        // Change tag displayed selecting appropriate tag from group
        window.contentView.tagGroup.setOnCheckedChangeListener { _, chosenTag ->
            selectedTag = when (chosenTag) {
                R.id.tagEvent -> Tag.EVENT
                R.id.tagBooking -> Tag.BOOKING
                R.id.tagBuy -> Tag.BUY
                else -> Tag.NONE
            }

            parent.setImageResourceFromTag(selectedTag)
            window.dismiss()
        }

        return window
    }
}
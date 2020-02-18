package com.example.myapplication.popup_windows

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import com.example.myapplication.R
import com.example.myapplication.data_classes.Tag
import com.example.myapplication.data_classes.setImageResourceFromTag
import kotlinx.android.synthetic.main.popup_tag.view.*

class PopupTag(private val parent: View, private val context: Context) : PopupWindowParent() {
    var selectedTag = Tag.NONE
    private set

    fun create(): PopupWindow {
        val window:PopupWindow = createAndShow(context, R.layout.popup_tag, parent, Anchor.BottomLeft)
        // Change tag displayed selecting appropriate tag from group
        window.contentView.tagGroup.setOnCheckedChangeListener { _, chosenTag ->
            selectedTag = when (chosenTag) {
                R.id.tagEvent -> Tag.EVENT
                R.id.tagBooking -> Tag.BOOKING
                R.id.tagBuy -> Tag.BUY
                else -> Tag.NONE
            }

            // If imageView type parent, we want to update tag icon to newly selected icon
            if (parent is ImageView) { parent.setImageResourceFromTag(selectedTag) }

            window.dismiss()
        }

        return window
    }
}
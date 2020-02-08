package com.example.myapplication.popup_windows

import android.content.Context
import android.widget.ImageView
import android.widget.PopupWindow
import com.example.myapplication.R
import com.example.myapplication.data_classes.Tag
import com.example.myapplication.data_classes.setImageResourceFromTag
import kotlinx.android.synthetic.main.tag_popup_window.view.*

fun Context.createTagPopup(parent: ImageView): PopupWindow {
    val timePopup:PopupWindow = this.createPopup(R.layout.tag_popup_window, parent)
    var tag: Tag

    timePopup.contentView.tagGroup.setOnCheckedChangeListener { _, chosenTag ->
        tag = when (chosenTag) {
            R.id.tagEvent -> Tag.EVENT
            R.id.tagBooking -> Tag.BOOKING
            R.id.tagBuy -> Tag.BUY
            else -> Tag.NONE
        }

        parent.setImageResourceFromTag(tag)
        timePopup.dismiss()
    }

    return timePopup
}
package com.example.myapplication.singletons

import android.content.Context
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.data_classes.*
import com.example.myapplication.popups.PopupDate
import com.example.myapplication.popups.PopupTag
import com.example.myapplication.popups.PopupTime

object PopupManager {
    private val tagsList: ArrayList<Int> = arrayListOf(
        R.drawable.tag_booking, R.drawable.tag_assignment, R.drawable.tag_mail, R.drawable.tag_file,
        R.drawable.tag_scan, R.drawable.tag_print, R.drawable.tag_bug, R.drawable.tag_build,

        R.drawable.tag_tv , R.drawable.tag_read, R.drawable.tag_music_note, R.drawable.tag_game,
        R.drawable.tag_photo, R.drawable.tag_movie, R.drawable.tag_food, R.drawable.tag_event,

        R.drawable.tag_buy, R.drawable.tag_pet, R.drawable.tag_workout, R.drawable.tag_medicine,
        R.drawable.tag_delivery, R.drawable.tag_flight, R.drawable.tag_train, R.drawable.tag_car,

        R.drawable.tag_important, R.drawable.tag_flag, R.drawable.tag_1, R.drawable.tag_2,
        R.drawable.tag_3, R.drawable.tag_4, R.drawable.tag_5, R.drawable.tag_6
    )

    var time: PopupTime = PopupTime()
    var tag: PopupTag = PopupTag(tagsList)
    var date: PopupDate = PopupDate()
}
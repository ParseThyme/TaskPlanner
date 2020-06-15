package com.example.myapplication.popups

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data_classes.Task
import com.example.myapplication.singletons.Settings
import com.example.myapplication.inflate
import com.example.myapplication.updateDrawableTop
import kotlinx.android.synthetic.main.popup_tag.view.*
import kotlinx.android.synthetic.main.popup_tag_entry.view.*

class PopupTag(private val tagsList: ArrayList<Int>) : Popup() {
    var update: Boolean = false

    fun create(edited: Task, context: Context, attachTo: View, modify: TextView? = null): PopupWindow {
        // Calculate number of icons per row. Ideally max is 10 per row.
        var spanCount: Int = Settings.tagRowSize
        if (tagsList.size < spanCount) spanCount = tagsList.size

        val window: PopupWindow = create(context, R.layout.popup_tag)
        val view: View = window.contentView

        update = false      // Input validation, true when tag chosen/removed, otherwise window clicked outside

        // Remove tag
        view.btnClearTag.setOnClickListener {
            modify?.updateDrawableTop(R.drawable.tag_base)
            edited.tag = R.drawable.tag_base
            update = true
            window.dismiss()
        }
        // Change tag
        view.tagsRv.apply {
            layoutManager = GridLayoutManager(context, spanCount)
            adapter = PopupTagAdapter(tagsList)
            // Select and close function passed into TaskTagAdapter
            { chosenTag: Int ->           // Input Param
                modify?.updateDrawableTop(chosenTag)
                edited.tag = chosenTag
                update = true
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

class PopupTagAdapter(private val tagsList: ArrayList<Int>, private val closeFn: (Int) -> Unit)
    : RecyclerView.Adapter<PopupTagAdapter.ViewHolder>()
{
    override fun getItemCount(): Int { return tagsList.size }
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) { holder.bind(tagsList[pos]) }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.popup_tag_entry))
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(entry: Int) {
            itemView.iconTag.apply {
                setImageResource(entry)                  // Set icon to match taskTag
                setOnClickListener { closeFn(entry) }    // When tag clicked, close popup window
            }
        }
    }
}
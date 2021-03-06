package com.example.myapplication.recyclerviewdecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LinearLayoutDecoration(private val spacing: Int) : RecyclerView.ItemDecoration()
{
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State ) {
        when (view.tag == "group") {
            true -> outRect.set(spacing, 0, spacing, spacing)     // Group = Pad left, right, bottom
            false -> outRect.set(0, 0, 0, spacing)     // Header = Only pad bottom
        }
    }
}
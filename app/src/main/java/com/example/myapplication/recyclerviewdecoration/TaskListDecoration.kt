package com.example.myapplication.recyclerviewdecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

// Source: https://gist.github.com/liangzhitao/e57df3c3232ee446d464
// Modified and edited to fit usage

class TaskListDecoration(private val spacing: Int, private val includeEdge: Boolean, private val spanCount: Int = 1)
    : RecyclerView.ItemDecoration()
{
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State ) {
        val position = parent.getChildAdapterPosition(view) // item position

        // If header, don't use padding
        if (view.tag == "header") { outRect.set(0, 0, 0, 0) }

        else if (position >= 0) {
            val column = position % spanCount // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)

                // top edge
                if (position < spanCount) {
                    outRect.top = spacing
                }
                outRect.bottom = spacing // item bottom
            } else {
                outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing // item top
                }
            }
        }
    }
}
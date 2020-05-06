package com.example.myapplication.recyclerviewdecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.utility.debugMessagePrint


// Grid spacing: https://stackoverflow.com/questions/56775671/get-the-column-number-where-a-view-is-on-a-gridlayoutmanager

class GridLayoutDecoration(private val spacing: Int, private val spanSize: Int) : RecyclerView.ItemDecoration()
{
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State ) {
        val column: Int = (view.layoutParams as GridLayoutManager.LayoutParams).spanIndex
        val matchingColumn: Int = spanSize - 1 // (spanSize - 1 as column starts at [0])

        when (view.tag == "header") {
            // Header, add padding at bottom
            true -> outRect.set(0, 0, 0, spacing)
            // Cell, add padding at bottom and left. Only add right padding at rightmost cells.
            false -> {
                outRect.top = 0             // Top padding applied from previous cell / header
                outRect.left = spacing
                outRect.bottom = spacing

                // If column matches spanSize, apply right padding
                if (column == matchingColumn)
                    outRect.right = spacing
            }
        }
    }
}
package com.example.myapplication.recyclerviewdecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.myapplication.utility.debugMessagePrint


// Grid spacing: https://stackoverflow.com/questions/56775671/get-the-column-number-where-a-view-is-on-a-gridlayoutmanager

class GridLayoutDecoration(private val spacing: Int, private val spanSize: Int) : RecyclerView.ItemDecoration()
{
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State ) {
        val layout: StaggeredGridLayoutManager.LayoutParams = (view.layoutParams as StaggeredGridLayoutManager.LayoutParams)

        when (view.tag == "group") {
            // Group. Add padding to bottom and left. Add right padding to rightmost entries
            true -> {
                val column: Int = layout.spanIndex
                val matchingColumn: Int = spanSize - 1 // (spanSize - 1 as column starts at [0])

                outRect.top = 0             // Top padding applied from previous cell / header
                outRect.left = spacing
                outRect.bottom = spacing

                // If column matches spanSize, apply right padding
                if (column == matchingColumn) outRect.right = spacing
            }
            // Header. Ensure takes up full width, add padding to bottom only
            false -> {
                outRect.set(0, 0, 0, spacing)
                layout.isFullSpan = true
            }
        }
    }
}
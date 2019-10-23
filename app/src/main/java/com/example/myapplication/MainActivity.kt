package com.example.myapplication

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView

// Resource: https://www.youtube.com/watch?v=EwwdQt3_fFU

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create reference to listview in activity_main.xml via id: taskList
        val listView = findViewById<ListView>(R.id.taskList)

        // Adapter to tell list view what to render
        listView.adapter = MyCustomerAdapter(this)
    }

    private class MyCustomerAdapter(context: Context): BaseAdapter() {

        private val dates = arrayListOf<String>(
            "OCT-23", "OCT-24"
        )

        private val mContext: Context

        init {
            mContext = context
        }

        // Number of rows in list
        override fun getCount(): Int {
            return dates.size
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getItem(position: Int): Any {
            return "Unused function"
        }

        // Rendering each row
        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            // Use custom layout for row
            val layoutInflater = LayoutInflater.from(mContext)
            val rowLayout = layoutInflater.inflate(R.layout.tasklist_row, viewGroup, false)

            // Update content of row
            val dateRow = rowLayout.findViewById<TextView>(R.id.date)
            dateRow.text = dates.get(position)

            /* Add new content to tasks
            val textRow = rowLayout.findViewById<TextView>(R.id.task)
            val oldTask = textRow.text
            val newTask = "\n* Task 2"
            var updatedTask = "$oldTask$newTask"
            textRow.text = updatedTask
            */

            return rowLayout
        }
    }
}

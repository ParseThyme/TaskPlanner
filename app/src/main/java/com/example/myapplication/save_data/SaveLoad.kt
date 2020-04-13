package com.example.myapplication.save_data

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.data_classes.TaskGroup
import com.example.myapplication.utility.Settings
import com.example.myapplication.utility.ViewLayout
import com.example.myapplication.utility.spName
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SaveLoad(context: Context) {
    private val sharedPref: SharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPref.edit()

    // Saved keys with corresponding values
    private val keyTaskGroupList: String = "taskGroupList"
    private val keyViewLayout: String = "mainLayout"

    // ####################
    // Save
    // ####################
    fun saveTaskGroupList(savedList: ArrayList<TaskGroup>) {
        // Create gson to convert data to json format
        val json: String = Gson().toJson(savedList)
        // Place data in editor then apply
        editor.putString(keyTaskGroupList, json)
        editor.apply()
    }
    // Save index(ordinal) of mainLayout
    fun saveLayout() { save(keyViewLayout, Settings.mainLayout.ordinal) }

    private fun save(keyName: String, value: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(keyName, value)
        editor.apply()
    }

    /*
    fun save(keyName: String, value: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(keyName, value)
        editor.apply()
    }

    fun save(keyName: String, value: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(keyName, value)
        editor.commit()
    }
    */

    // ####################
    // Load
    // ####################
    fun loadTaskGroupList() : ArrayList<TaskGroup> {
        val json: String? = sharedPref.getString(keyTaskGroupList, null)
        // Using util function to convert data to json
        val savedData: ArrayList<TaskGroup> = Gson().fromJson(json)

        // Check for existing data, if so return it
        if (!savedData.isNullOrEmpty()) { return savedData }

        // Otherwise return new list
        return ArrayList()
    }
    // When loading mainLayout, convert ordinal to appropriate layout enum value
    fun loadLayout() : ViewLayout {
        // Values: 0 == ViewLayout.LINEAR, 1 == ViewLayout.GRID. Default to linear
        val loadedLayout: Int = sharedPref.getInt(keyViewLayout, 0)
        return ViewLayout.values()[loadedLayout]
    }

    /*
    fun loadString(keyName: String) : String? {
        return sharedPref.getString(keyName, null)
    }

    fun loadInt(keyName: String) : Int? {
        return sharedPref.getInt(keyName, 0)
    }

    fun loadBoolean(keyName: String) : Boolean? {
        return sharedPref.getBoolean(keyName, null)
    }
    */

    fun clearAllData() {
        editor.clear()
        editor.apply()
    }

    /*
    fun deleteData(keyName: String) {
        editor.remove(keyName)
        editor.apply()
    }
    */
}

// https://stackoverflow.com/questions/33381384/how-to-use-typetoken-generics-with-gson-in-kotlin
inline fun <reified T> Gson.fromJson(json: String?): T = this.fromJson<T>(json, object: TypeToken<T>() {}.type)

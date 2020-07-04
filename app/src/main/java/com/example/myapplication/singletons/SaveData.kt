package com.example.myapplication.singletons

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.ViewLayout
import com.example.myapplication.data_classes.GroupEntry
import com.example.myapplication.data_classes.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SaveData {
    // SharedPreferences (Saving/Loading data)
    private const val spName: String = "SavedData"
    // Saved keys with corresponding values
    private const val keyTaskGroupList: String = "taskGroupList"
    private const val keyViewLayout: String = "mainLayout"
    private const val keyTimeDelta: String = "timeDelta"
    private const val keyStoredTaskList: String = "savedTasks"

    // Saved preferences editor
    private fun Context.getEditor() : SharedPreferences.Editor {
        val sharedPref: SharedPreferences = getSharedPreferences(spName, Context.MODE_PRIVATE)
        return sharedPref.edit()
    }

    // ####################
    // Save
    // ####################
    fun saveTaskGroupList(taskGroupList: ArrayList<GroupEntry>, context: Context) {
        // Get editor, create gson to convert data to json format
        val editor: SharedPreferences.Editor = context.getEditor()
        val json: String = Gson().toJson(taskGroupList)

        // Place data in editor then apply
        editor.putString(keyTaskGroupList, json)
        editor.apply()
    }
    fun saveStoredTaskList(storedTaskList: ArrayList<Task>, context: Context) {
        val editor: SharedPreferences.Editor = context.getEditor()
        val json: String = Gson().toJson(storedTaskList)

        editor.putString(keyStoredTaskList, json)
        editor.apply()
    }
    // index(ordinal) of mainLayout
    fun saveLayout(context: Context) {
        save(keyViewLayout, Settings.mainLayout.ordinal, context)
    }
    fun saveTimeDelta(context: Context) {
        save(keyTimeDelta, Settings.timeDelta, context)
    }

    private fun save(keyName: String, value: Int, context: Context) {
        val editor: SharedPreferences.Editor = context.getEditor()
        editor.putInt(keyName, value)
        editor.apply()
    }

    // ####################
    // Load
    // ####################
    fun loadTaskGroupList(context: Context) : ArrayList<GroupEntry> {
        val sharedPref: SharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
        val json: String? = sharedPref.getString(keyTaskGroupList, null)
        // Using util function to convert data to json
        val savedData: ArrayList<GroupEntry> = Gson().fromJson(json)
        // Check for existing data, if so return it
        return when (savedData.isNullOrEmpty()) {
            true -> arrayListOf()               // No data exists, return new list
            false -> savedData                  // Previous data found, return saved data
        }
    }
    fun loadStoredTaskList(context: Context) : ArrayList<Task> {
        val sharedPref: SharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
        val json: String? = sharedPref.getString(keyStoredTaskList, null)
        // Using util function to convert data to json
        val savedData: ArrayList<Task> = Gson().fromJson(json)
        // Check for existing data, if so return it
        return when (savedData.isNullOrEmpty()) {
            true -> arrayListOf()               // No data exists, return new list
            false -> savedData                  // Previous data found, return saved data
        }
    }
    // When loading mainLayout, convert ordinal to appropriate layout enum value
    fun loadLayout(context: Context) : ViewLayout {
        // Values: 0 == ViewLayout.LINEAR, 1 == ViewLayout.GRID. Default to linear
        val sharedPref: SharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
        val loadedLayout: Int = sharedPref.getInt(keyViewLayout, 0)
        return ViewLayout.values()[loadedLayout]
    }
    fun loadTimeDelta(context: Context) : Int {
        val sharedPref: SharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
        return sharedPref.getInt(
            keyTimeDelta,
            Settings.defTimeDelta
        )
    }

    /*
    fun clearAllData() {
        editor.clear()
        editor.apply()
    }

    fun deleteData(keyName: String) {
        editor.remove(keyName)
        editor.apply()
    }
    */

    // https://stackoverflow.com/questions/33381384/how-to-use-typetoken-generics-with-gson-in-kotlin
    private inline fun <reified T> Gson.fromJson(json: String?): T = this.fromJson<T>(json, object: TypeToken<T>() {}.type)
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

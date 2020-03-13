package com.example.myapplication.utility

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.data_classes.TaskGroup
import com.google.gson.Gson

class SaveLoad(context: Context) {
    private val sharedPref: SharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPref.edit()

    fun saveTaskGroupList(listToSave: ArrayList<TaskGroup>) {
        // Create gson to convert data to json format
        val json: String = Gson().toJson(listToSave)
        // Place data in editor then apply
        editor.putString(spTaskGroupList, json)
        editor.apply()
    }

    /*
    fun save(keyName: String, value: ArrayList<TaskGroup>) {
        // Create gson to convert data to json format
        val json: String = Gson().toJson(value)
        // Place data in editor then apply
        editor.putString(keyName, json)
        editor.apply()
    }

    fun save(keyName: String, value: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(keyName, value)
        editor.apply()
    }
    fun save(keyName: String, value: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(keyName, value)
        editor.commit()
    }

    fun save(keyName: String, value: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(keyName, value)
        editor.commit()
    }
    */

    fun loadTaskGroupList() : ArrayList<TaskGroup> {
        val json: String? = sharedPref.getString(spTaskGroupList, null)
        // Using util function to convert data to json (see util class)
        val savedData: ArrayList<TaskGroup> = Gson().fromJson(json)

        // Check for existing data, if so return it
        if (!savedData.isNullOrEmpty()) {
            return savedData
        }

        // Otherwise return new list
        return ArrayList()
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
package com.planscollective.plansapp.helper

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.planscollective.plansapp.PLANS_APP

object PrefHelper {
    private const val PREF_MODE = Context.MODE_PRIVATE
    private const val PREF_NAME = "PreferenceInfo"

    val sharedPref : SharedPreferences?
        get() = PLANS_APP.applicationContext?.getSharedPreferences(PREF_NAME, PREF_MODE)

    fun readPrefBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return try {
            sharedPref?.getBoolean(key, defaultValue) ?: defaultValue
        }catch (e: Exception) {
            defaultValue
        }
    }

    fun savePrefBoolean(key: String, value: Boolean) {
        sharedPref?.apply {
            edit().putBoolean(key, value).apply()
        }
    }

    fun readPrefString(key: String, defValue: String? = null): String? {
        return try {
            sharedPref?.getString(key, defValue) ?: defValue
        }catch (e: Exception) {
            defValue
        }
    }

    fun savePrefString(key: String, value: String?) {
        sharedPref?.apply {
            edit().putString(key, value).apply()
        }
    }

    fun readPrefInt(key: String, defaultValue: Int = 0): Int {
        return try {
            sharedPref?.getInt(key, defaultValue) ?: defaultValue
        }catch (e: Exception) {
            defaultValue
        }
    }

    fun savePrefInt(key: String, value: Int) {
        sharedPref?.apply {
            edit().putInt(key, value).apply()
        }
    }

    fun readPrefLong( key: String, defaultValue: Long = 0): Long {
        return try{
            sharedPref?.getLong(key, defaultValue) ?: defaultValue
        }catch (e : Exception) {
            defaultValue
        }
    }

    fun savePrefLong(key: String, value: Long) {
        sharedPref?.apply {
            edit().putLong(key, value).apply()
        }
    }

    fun readPrefFloat( key: String, defaultValue: Float = 0F): Float {
        return try {
            sharedPref?.getFloat(key, defaultValue) ?: defaultValue
        }catch (e: Exception) {
            defaultValue
        }
    }

    fun savePrefFloat(key: String, value: Float) {
        sharedPref?.apply {
            edit().putFloat(key, value).apply()
        }
    }

    fun readPrefDouble( key: String, defaultValue: Double = 0.0): Double {
        return readPrefFloat(key, defaultValue.toFloat()).toDouble()
    }

    fun savePrefDouble(key: String, value: Double) {
        savePrefFloat(key, value.toFloat())
    }

    inline fun <reified T> readPrefObject(key: String, defValue: T? = null): T? {
        return sharedPref?.getString(key, null)?.let {
            try {
                Gson().fromJson(it, object: TypeToken<T>(){}.type)
            }catch (e : Exception) {
                e.printStackTrace()
                defValue
            }
        }
    }

    fun savePrefObject(key: String, value: Any?) {
        sharedPref?.apply {
            val json = Gson().toJson(value) ?: null
            edit().putString(key, json).apply()
        }
    }


}
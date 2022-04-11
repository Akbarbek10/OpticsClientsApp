package com.example.opticsclientsapp.shared_preference

import android.content.Context
import android.content.SharedPreferences
import com.example.opticsclientsapp.models.enum.AppLanguage


class Pref {
    private val NAME = "MyPref"
    private val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var nightMode: Boolean
        get() = preferences.getBoolean("night_mode", false)
        set(value) = preferences.edit {
            it.putBoolean("night_mode" , value)
        }

    var notificationEnabled: Boolean
        get() = preferences.getBoolean("notification_enabled", true)
        set(value) = preferences.edit {
            it.putBoolean("notification_enabled" , value)
        }

    var chosenLang: String?
        get() = preferences.getString("lang", AppLanguage.RUSSIAN.langAbbr)
        set(value) = preferences.edit {
            it.putString("lang", value)
        }

    var chosenLangId: Int
        get() = preferences.getInt("lang_id", AppLanguage.RUSSIAN.ordinal)
        set(value) = preferences.edit {
            it.putInt("lang_id", value)
        }

    var sortPosition: Int
        get() = preferences.getInt("sort_position", 0)
        set(value) = preferences.edit {
            it.putInt("sort_position", value)
        }

    var isOpenedFirstTime: Boolean
        get() = preferences.getBoolean("app_opened", true)
        set(value) = preferences.edit {
            it.putBoolean("app_opened" , value)
        }

}
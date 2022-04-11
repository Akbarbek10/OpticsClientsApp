package com.example.opticsclientsapp.object_class

import android.content.Context
import android.content.res.Configuration
import java.util.*


object LocaleHelper {

    fun setLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.locale = locale

        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

}
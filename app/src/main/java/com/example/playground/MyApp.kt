package com.example.playground

import android.app.Application
import android.content.Context

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this.applicationContext
    }

    companion object {
        var appContext: Context? = null
    }
}
package com.example.myapp

import android.app.Application
import android.util.Log

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize app-level components here
        Log.d("AppApplication", "Application started")
    }
}
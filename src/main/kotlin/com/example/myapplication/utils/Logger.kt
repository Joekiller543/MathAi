package com.example.myapplication.utils

import android.util.Log

object AppLogger {
    private const val TAG = "MyApp"

    fun debug(message: String) {
        Log.d(TAG, message)
    }

    fun error(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}
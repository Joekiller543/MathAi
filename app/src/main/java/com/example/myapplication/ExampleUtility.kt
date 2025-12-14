package com.example.myapplication

object ExampleUtility {
    fun getWelcomeMessage(appName: String): String {
        return "Welcome to the clean project: $appName"
    }

    fun addNumbers(a: Int, b: Int): Int {
        return a + b
    }
}
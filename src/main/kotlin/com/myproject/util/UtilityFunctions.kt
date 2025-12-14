package com.myproject.util

fun logMessage(tag: String, message: String) {
    println("[$tag] $message")
}

fun formatString(input: String): String {
    return input.trim().uppercase()
}
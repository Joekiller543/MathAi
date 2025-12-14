package com.example.myapp.util

import android.content.Context
import java.io.File

/**
 * Manages application-specific files and directories.
 * Replaces deprecated StorageUtil.kt functionality.
 */
class AppFileManager(private val context: Context) {

    /**
     * Returns the app-specific directory for the given name, creating it if it doesn't exist.
     */
    fun getAppSpecificDirectory(directoryName: String): File {
        val appDir = File(context.filesDir, directoryName)
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        return appDir
    }

    /**
     * Saves content to a file within a specified app-specific directory.
     */
    fun saveFile(directoryName: String, fileName: String, content: ByteArray): File {
        val directory = getAppSpecificDirectory(directoryName)
        val file = File(directory, fileName)
        file.writeBytes(content)
        return file
    }

    /**
     * Reads content from a file within a specified app-specific directory.
     */
    fun readFile(directoryName: String, fileName: String): ByteArray? {
        val directory = getAppSpecificDirectory(directoryName)
        val file = File(directory, fileName)
        return if (file.exists()) file.readBytes() else null
    }

    /**
     * Deletes a file from a specified app-specific directory.
     */
    fun deleteFile(directoryName: String, fileName: String): Boolean {
        val directory = getAppSpecificDirectory(directoryName)
        val file = File(directory, fileName)
        return file.delete()
    }
}

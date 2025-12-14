File: app/src/main/java/com/example/myapplication/util/AppFileManager.kt
```kotlin
package com.example.myapplication.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Single source of truth for File System operations.
 *
 * Consolidates directory creation and file writing logic, handling the complexity
 * of Android's Scoped Storage (API 29+) vs Legacy Storage (API < 29).
 *
 * Replaces functionality previously found in StorageUtil.
 */
object AppFileManager {

    private const val TAG = "AppFileManager"
    private const val DIRECTORY_NAME = "MathAssistant"

    /**
     * Attempts to create the application's working directory.
     *
     * Strategies:
     * 1. App-Specific External Storage: Created by default as a reliable fallback.
     * 2. Public Documents (Legacy): Explicitly created on devices < API 29.
     * 3. Scoped Storage: Folders are virtual; creation is implied during file insertion.
     *
     * @return True if at least one storage location is ready/writable.
     */
    fun createAppFolder(context: Context): Boolean {
        var isSuccess = false

        // 1. Ensure App-Specific Storage exists (Android/data/...)
        // This acts as our internal working cache and requires no permissions.
        try {
            val appSpecificDir = getAppSpecificDir(context)
            if (!appSpecificDir.exists()) {
                if (appSpecificDir.mkdirs()) {
                    Log.d(TAG, "App-Specific folder created: ${appSpecificDir.absolutePath}")
                    isSuccess = true
                }
            } else {
                isSuccess = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize app-specific storage", e)
        }

        // 2. Handle Legacy Public Storage (Pre-Android Q)
        // We explicitly create the folder so users can see it in their file manager immediately.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                val publicDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    DIRECTORY_NAME
                )
                if (!publicDir.exists()) {
                    if (publicDir.mkdirs()) {
                        Log.d(TAG, "Legacy public folder created: ${publicDir.absolutePath}")
                        isSuccess = true
                    }
                } else {
                    isSuccess = true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to create legacy public folder (Permission might be denied)", e)
                // We don't return false here if app-specific storage worked.
            }
        } else {
            // On Android Q+, we don't create an empty public folder explicitly via File API
            // because strict Scoped Storage usually prevents it without SAF.
            // We rely on saveTextFile to create the structure on demand.
        }

        return isSuccess
    }

    /**
     * Saves a text file, automatically selecting the correct storage API based on Android Version.
     *
     * @param context Application context.
     * @param fileName The name of the file (e.g., "learning_plan.txt").
     * @param content The text content to write.
     * @return True if successful.
     */
    fun saveTextFile(context: Context, fileName: String, content: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToScopedStorage(context, fileName, content)
        } else {
            saveToLegacyStorage(context, fileName, content)
        }
    }

    /**
     * Implementation for Android 10 (API 29) and above using MediaStore.
     * No Write Permission required for files owned by this app.
     */
    private fun saveToScopedStorage(context: Context, fileName: String, content: String): Boolean {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/$DIRECTORY_NAME")
                put(MediaStore.MediaColumns.IS_PENDING, 1) // Exclusive access while writing
            }

            // Select external volume
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Files.getContentUri("external")
            }

            val uri = context.contentResolver.insert(collection, contentValues)
                ?: throw IOException("Failed to create MediaStore entry")

            // Write data
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }

            // Publish file (remove pending status)
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            context.contentResolver.update(uri, contentValues, null, null)

            Log.d(TAG, "Scoped Storage: Saved $fileName to Documents/$DIRECTORY_NAME")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Scoped Storage Error: ${e.message}", e)
            // Fallback to app-specific storage if MediaStore fails
            return saveToAppSpecificStorage(context, fileName, content)
        }
    }

    /**
     * Implementation for Android 9 (API 28) and below using standard File API.
     * Requires WRITE_EXTERNAL_STORAGE permission.
     */
    private fun saveToLegacyStorage(context: Context, fileName: String, content: String): Boolean {
        try {
            val publicDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                DIRECTORY_NAME
            )

            if (!publicDir.exists() && !publicDir.mkdirs()) {
                Log.w(TAG, "Legacy Storage: Could not create directory. Trying fallback.")
                return saveToAppSpecificStorage(context, fileName, content)
            }

            val file = File(publicDir, fileName)
            FileOutputStream(file).use { it.write(content.toByteArray()) }

            Log.d(TAG, "Legacy Storage: Saved $fileName at ${file.absolutePath}")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Legacy Storage Error: ${e.message}", e)
            return saveToAppSpecificStorage(context, fileName, content)
        }
    }

    /**
     * Reliable fallback: Internal App-Specific Storage.
     * Files here are private to the app and deleted on uninstall.
     */
    private fun saveToAppSpecificStorage(context: Context, fileName: String, content: String): Boolean {
        return try {
            val dir = getAppSpecificDir(context)
            if (!dir.exists() && !dir.mkdirs()) return false

            val file = File(dir, fileName)
            FileOutputStream(file).use { it.write(content.toByteArray()) }

            Log.d(TAG, "Fallback Storage: Saved $fileName at ${file.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Fallback Storage Error: ${e.message}", e)
            false
        }
    }

    private fun getAppSpecificDir(context: Context): File {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), DIRECTORY_NAME)
    }
}
```
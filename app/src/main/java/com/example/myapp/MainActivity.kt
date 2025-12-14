package com.example.myapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapp.util.AppFileManager

class MainActivity : AppCompatActivity() {
    private lateinit var appFileManager: AppFileManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appFileManager = AppFileManager(this)

        // Example usage of AppFileManager
        val fileName = "my_data.txt"
        val directoryName = "documents"
        val content = "Hello from AppFileManager!".toByteArray()

        appFileManager.saveFile(directoryName, fileName, content)
        val readContent = appFileManager.readFile(directoryName, fileName)
        readContent?.let { 
            // Log.d("MainActivity", String(it))
        }
    }
}

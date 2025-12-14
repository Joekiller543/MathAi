package com.example.mathassistant

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.mathassistant.navigation.AppNavHost
import com.example.mathassistant.ui.theme.MathAssistantTheme

@Composable
fun MathAssistantApp() {
    MathAssistantTheme {
        val navController = rememberNavController()
        AppNavHost(navController = navController)
    }
}
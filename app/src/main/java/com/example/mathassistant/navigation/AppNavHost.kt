package com.example.mathassistant.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mathassistant.features.calculator.ui.CalculatorScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Calculator.route
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Calculator.route) {
            CalculatorScreen()
        }
        // Define other screens here
    }
}

sealed class Screen(val route: String) {
    object Calculator : Screen("calculator_screen")
    // Add more screens as needed
}
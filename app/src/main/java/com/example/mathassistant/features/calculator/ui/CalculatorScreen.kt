package com.example.mathassistant.features.calculator.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    Text(text = "Calculator Screen Content. Result: ${viewModel.calculationResult}")
    // UI for calculator
}
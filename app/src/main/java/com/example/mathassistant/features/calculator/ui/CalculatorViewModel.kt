package com.example.mathassistant.features.calculator.ui

import androidx.lifecycle.ViewModel
import com.example.mathassistant.features.calculator.data.CalculatorRepository

class CalculatorViewModel(private val repository: CalculatorRepository = CalculatorRepository()) : ViewModel() {
    var calculationResult: String = "0"
        private set

    fun performCalculation(expression: String) {
        calculationResult = repository.calculate(expression)
    }
}
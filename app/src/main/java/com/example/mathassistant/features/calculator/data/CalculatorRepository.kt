package com.example.mathassistant.features.calculator.data

import com.example.mathassistant.features.calculator.data.local.CalculationDataSource

class CalculatorRepository(private val dataSource: CalculationDataSource = CalculationDataSource()) {
    fun calculate(expression: String): String {
        // In a real app, this would use the dataSource to perform/store calculations
        return dataSource.evaluate(expression)
    }
}
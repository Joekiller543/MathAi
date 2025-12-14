package com.example.myapplication

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme

object CalculatorLogic {
    fun add(a: Int, b: Int): Int = a + b
    fun subtract(a: Int, b: Int): Int = a - b
    fun multiply(a: Int, b: Int): Int = a * b
    fun divide(a: Int, b: Int): Int = if (b != 0) a / b else throw IllegalArgumentException("Cannot divide by zero")
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = "Calculator Feature")
        Text(text = "5 + 3 = ${CalculatorLogic.add(5, 3)}")
        Text(text = "10 - 4 = ${CalculatorLogic.subtract(10, 4)}")
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorScreenPreview() {
    MyApplicationTheme {
        CalculatorScreen()
    }
}
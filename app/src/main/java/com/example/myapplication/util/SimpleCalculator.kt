File: app/src/main/java/com/example/myapplication/util/SimpleCalculator.kt
```kotlin
package com.example.myapplication.util

import java.text.DecimalFormat
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan

/**
 * Utility class to evaluate mathematical expressions passed as strings.
 * Supports basic arithmetic (+, -, *, /), exponents (^), and trigonometric functions (sin, cos, tan).
 * Uses a Recursive Descent Parser approach.
 */
object SimpleCalculator {

    /**
     * Evaluates a mathematical expression string.
     *
     * @param str The expression to evaluate (e.g., "3+5*2", "sin(30)").
     * @return The calculated result as a String, or "Error" if parsing fails.
     */
    fun evaluate(str: String): String {
        if (str.isBlank()) return ""
        return try {
            val result = object : Any() {
                var pos = -1
                var ch = -1

                fun nextChar() {
                    ch = if (++pos < str.length) str[pos].code else -1
                }

                fun eat(charToEat: Int): Boolean {
                    while (ch == ' '.code) nextChar()
                    if (ch == charToEat) {
                        nextChar()
                        return true
                    }
                    return false
                }

                fun parse(): Double {
                    nextChar()
                    val x = parseExpression()
                    if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                    return x
                }

                // Grammar:
                // expression = term | expression `+` term | expression `-` term
                // term       = factor | term `*` factor | term `/` factor
                // factor     = `+` factor | `-` factor | `(` expression `)`
                //            | number | functionName factor | factor `^` factor

                fun parseExpression(): Double {
                    var x = parseTerm()
                    while (true) {
                        if (eat('+'.code)) x += parseTerm() // addition
                        else if (eat('-'.code)) x -= parseTerm() // subtraction
                        else return x
                    }
                }

                fun parseTerm(): Double {
                    var x = parseFactor()
                    while (true) {
                        if (eat('*'.code)) x *= parseFactor() // multiplication
                        else if (eat('/'.code)) x /= parseFactor() // division
                        else return x
                    }
                }

                fun parseFactor(): Double {
                    if (eat('+'.code)) return parseFactor() // unary plus
                    if (eat('-'.code)) return -parseFactor() // unary minus

                    var x: Double
                    val startPos = pos
                    if (eat('('.code)) { // parentheses
                        x = parseExpression()
                        eat(')'.code)
                    } else if (ch in '0'.code..'9'.code || ch == '.'.code) { // numbers
                        while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                        x = str.substring(startPos, pos).toDouble()
                    } else if (ch in 'a'.code..'z'.code) { // functions
                        while (ch in 'a'.code..'z'.code) nextChar()
                        val func = str.substring(startPos, pos)
                        x = parseFactor()
                        x = when (func) {
                            "sqrt" -> sqrt(x)
                            "sin" -> sin(Math.toRadians(x))
                            "cos" -> cos(Math.toRadians(x))
                            "tan" -> tan(Math.toRadians(x))
                            else -> throw RuntimeException("Unknown function: $func")
                        }
                    } else {
                        throw RuntimeException("Unexpected: " + ch.toChar())
                    }

                    if (eat('^'.code)) x = x.pow(parseFactor()) // exponentiation

                    return x
                }
            }.parse()

            formatResult(result)
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun sqrt(x: Double): Double = kotlin.math.sqrt(x)

    /**
     * Formats the double result to remove unnecessary decimal points.
     * e.g., 4.0 -> "4", 4.123 -> "4.123"
     */
    private fun formatResult(value: Double): String {
        if (value.isInfinite() || value.isNaN()) return "Error"
        
        // Check if it's effectively an integer
        val longVal = value.toLong()
        return if (value == longVal.toDouble()) {
            longVal.toString()
        } else {
            // Limit decimal places for display
            val df = DecimalFormat("#.########")
            df.format(value)
        }
    }
}
```
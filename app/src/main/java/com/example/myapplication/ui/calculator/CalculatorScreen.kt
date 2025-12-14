Here are the complete Kotlin files for the migrated Calculator feature. I have extracted the `ScientificCalculator` UI component into its own file within the feature package to make it reusable and self-contained, ensuring the Navigation Graph references resolve correctly.

File: app/src/main/java/com/example/myapplication/features/calculator/ui/ScientificCalculator.kt
```kotlin
package com.example.myapplication.features.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A reusable Scientific Calculator UI component.
 * Can be used as a standalone screen or embedded within other screens (like LearningScreen).
 */
@Composable
fun ScientificCalculator(
    expression: String,
    result: String,
    onInput: (String) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 16.dp,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier
            .fillMaxWidth()
            // In standalone mode, this might take full height, but usually defined by parent
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Calculator Handle / Header / Close Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClose() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Display Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = expression.ifEmpty { "0" },
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = result,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Keypad Layout
            val buttons = listOf(
                listOf("C", "(", ")", "/"),
                listOf("sin", "cos", "tan", "*"),
                listOf("7", "8", "9", "-"),
                listOf("4", "5", "6", "+"),
                listOf("1", "2", "3", "^"),
                listOf("0", ".", "DEL", "=")
            )

            Column(
                modifier = Modifier.weight(1f, fill = false), // fill=false allows it to be used in scrollable parents
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                buttons.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { label ->
                            // Determine button styling based on type
                            val isOp = label in listOf("/", "*", "-", "+", "^", "=")
                            val isFunc = label in listOf("sin", "cos", "tan", "C", "DEL", "(", ")")
                            
                            val btnColor = when {
                                label == "=" -> MaterialTheme.colorScheme.primary
                                isOp -> MaterialTheme.colorScheme.secondaryContainer
                                isFunc -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.surfaceContainerHigh
                            }
                            
                            val textColor = when {
                                label == "=" -> MaterialTheme.colorScheme.onPrimary
                                isOp -> MaterialTheme.colorScheme.onSecondaryContainer
                                isFunc -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Button(
                                onClick = { onInput(label) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = btnColor,
                                    contentColor = textColor
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
```

File: app/src/main/java/com/example/myapplication/features/calculator/ui/CalculatorScreen.kt
```kotlin
package com.example.myapplication.features.calculator.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * The main screen for the Calculator feature, primarily used by the Navigation Host.
 * Wrapper around the [ScientificCalculator] component.
 */
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.BottomCenter // Align to bottom like a sheet, or Center for full screen
        ) {
            ScientificCalculator(
                expression = uiState.expression,
                result = uiState.result,
                onInput = viewModel::onInput,
                onClose = onClose
            )
        }
    }
}
```

File: app/src/main/java/com/example/myapplication/features/calculator/ui/CalculatorViewModel.kt
```kotlin
package com.example.myapplication.features.calculator.ui

import androidx.lifecycle.ViewModel
import com.example.myapplication.features.calculator.data.CalculatorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UI State for the Calculator.
 */
data class CalculatorUiState(
    val expression: String = "",
    val result: String = ""
)

/**
 * ViewModel for the Calculator feature.
 * Handles the logic for processing inputs and managing the calculation state.
 */
class CalculatorViewModel(private val repository: CalculatorRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun onInput(input: String) {
        val currentExpression = _uiState.value.expression
        val currentResult = _uiState.value.result

        when (input) {
            "C" -> {
                // Clear All
                _uiState.value = CalculatorUiState() 
            }
            "DEL" -> {
                // Backspace
                if (currentExpression.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        expression = currentExpression.dropLast(1)
                    )
                }
            }
            "=" -> {
                // Calculate
                if (currentExpression.isNotEmpty()) {
                    val result = repository.calculate(currentExpression)
                    _uiState.value = _uiState.value.copy(result = result)
                }
            }
            else -> {
                // If a calculation was just performed and user types a number, start new.
                // If user types an operator, append to previous expression (logic can vary).
                // For simplicity here, we just append to expression.
                
                // Handle functions that need parenthesis
                val toAppend = if (input in listOf("sin", "cos", "tan", "sqrt")) "$input(" else input
                _uiState.value = _uiState.value.copy(expression = currentExpression + toAppend)
            }
        }
    }
}
```

File: app/src/main/java/com/example/myapplication/features/calculator/data/CalculatorRepository.kt
```kotlin
package com.example.myapplication.features.calculator.data

import com.example.myapplication.features.calculator.data.local.CalculationDataSource

/**
 * Repository for the Calculator feature.
 * Abstracts the data source from the ViewModel.
 */
class CalculatorRepository(private val dataSource: CalculationDataSource) {
    
    fun calculate(expression: String): String {
        return dataSource.evaluate(expression)
    }
}
```

File: app/src/main/java/com/example/myapplication/features/calculator/data/local/CalculationDataSource.kt
```kotlin
package com.example.myapplication.features.calculator.data.local

import com.example.myapplication.util.SimpleCalculator

/**
 * Local Data Source for calculations.
 * Wraps the [SimpleCalculator] utility to provide a consistent data interface.
 */
class CalculationDataSource {
    
    fun evaluate(expression: String): String {
        return SimpleCalculator.evaluate(expression)
    }
}
```
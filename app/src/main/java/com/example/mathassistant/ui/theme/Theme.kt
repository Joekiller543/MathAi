package com.example.mathassistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun MathAssistantTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme, // Or LightColorScheme based on preference
        typography = Typography,
        content = content
    )
}
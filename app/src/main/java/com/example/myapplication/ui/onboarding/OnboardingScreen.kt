package com.example.myapplication.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel
) {
    Column(modifier = modifier) {
        Text(text = "Welcome to Onboarding!")
        // TODO: Implement onboarding steps and UI
    }
}
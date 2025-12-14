package com.example.myapplication

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun OnboardingScreen(onFinishOnboarding: () -> Unit) {
    Column {
        Text("Welcome to Math Assistant!")
        Text("Let's get you set up.")
        Button(onClick = onFinishOnboarding) {
            Text("Start Learning")
        }
    }
}